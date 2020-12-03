package net.mycorp.jimin.base.common.services;

import java.util.List;

import org.jooq.Record;
import org.jooq.Table;
import org.springframework.stereotype.Service;

import net.mycorp.jimin.base.core.OccamException;
import net.mycorp.jimin.base.domain.OcContext;
import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResult;

@Service
public class Nodes extends Bases {

	private Table<Record> thisTable() {
		return table(getResource().getTable());
	}

	private void updateHasChildren(OcMap row) {
		if (row == null || row.get("parent_id") == null || !datas.existsColumn(getResource(), "has_children"))
			return;
		
		boolean hasChildren = dsl()
				.fetchExists(dsl().selectOne().from(thisTable()).where(field("parent_id").eq(row.get("parent_id"))));
		dsl().update(thisTable()).set(field("has_children"), hasChildren).where(field("id").eq(row.get("parent_id")))
				.execute();
	}
	
	private void updateHasChildrenTrue(OcMap row) {
		if (row.get("parent_id") == null || !datas.existsColumn(getResource(), "has_children"))
			return;
		dsl().update(thisTable()).set(field("has_children"), true).where(field("id").eq(row.get("parent_id")))
				.execute();
	}
	
	public void postInsert(OcContext ctx) {
		OcMap row = ctx.getRow();
		updateHasChildrenTrue(row);
		super.postInsert(ctx);
	}
	
	@Override
	public void preUpdate(OcContext ctx) {
		OcMap row = ctx.getRow();
		if(row.get("parent_id") != null) {
			if(row.get("parent_id").equals(ctx.id())) {
				throw new OccamException("자기 자신을 상위로 지정할 수 없습니다.");
			}
			OcMap old = getOld(ctx);
			if(!row.get("parent_id").equals(old.get("parent_id"))) {
				updateHasChildren(old);
				updateHasChildrenTrue(row);
			}
		}
		updateSiblingsSeq(ctx);
		super.preUpdate(ctx);
	}
	
	public void preDelete(OcContext ctx) {
		OcMap old = getOld(ctx);
		updateHasChildren(old);
		super.preDelete(ctx);
	}
	
	@Override
	public void preSave(OcContext ctx) {
		OcMap row = ctx.getRow();
		if(row.containsKey("parent_id") && row.get("parent_id") == null) {
			throw new OccamException("parent_id can not be null");
		}
		if(row.get("parent_id") != null && row.get("parent_id").equals(row.id())) {
			throw new OccamException("parent_id and id can not be same");
		}
		super.preSave(ctx);
	}
	
	protected void updatePath(OcContext ctx, String nameField, String pathField, String separator) {
		OcMap row = ctx.row();
		if (row.get("parent_id") != null || row.get(nameField) != null) {
			if (ctx.id() != null) {
				// 업데이트
				OcMap old = getOld(ctx);
				if (old == null)
					return;
				if (row.get("parent_id") == null)
					row.put("parent_id", old.get("parent_id"));
				if (row.get(nameField) == null)
					row.put(nameField, old.get(nameField));
				if (!old.eq("parent_id", row.get("parent_id")) || !old.eq(nameField, row.get(nameField))) {
					// 수정된 경우
					row.put("id", ctx.id());
					if (row.get(nameField) == null) {
						row.put(nameField, old.get(nameField));
					}
					updatePath(row, nameField, pathField, separator);
					updatePathChildren(row, nameField, pathField, separator);
				}
			} else {
				// 신규
				updatePath(row, nameField, pathField, separator);
			}
		}
	}
	
	private void updatePath(OcMap row, String nameField, String pathField, String separator) {
		String parentId = row.getString("parent_id");
		if(parentId == null || parentId.equals("root")) {
			row.put(pathField, row.getString(nameField, ""));
		} else {
			Object parentPath;
			if(pathField.equals("id")) {
				parentPath = parentId;
			} else {
				OcResult parent = select(select(pathField).condition("id", parentId));
				if(parent.getData().size() == 0)
					throw new OccamException("parent %s %s not found.", resourceName, parentId);
				parentPath = parent.getValue();
			}
			row.put(pathField, parentPath == null ? row.get(nameField) : parentPath + separator + row.get(nameField));
		}
	}

	private void updatePathChildren(OcMap parent, String nameField, String pathField, String separator) {
		if(parent.get("id") == null)
			return;
		List<OcMap> children = select(select("id,"+nameField+","+pathField).condition("parent_id", parent.get("id"))).getData();
		for (OcMap child : children) {
			child.put(pathField, parent.get(pathField) + separator + child.get(nameField));
			update(child.get("id"), m(pathField, child.get(pathField)));
			updatePathChildren(child, nameField, pathField, separator);
		}
	}
	
	private void updateSiblingsSeq(OcContext ctx) {
		OcMap row = ctx.getRow();
		if (!row.getBool("_update_seq"))
			return;
		
		String keyExpr = row.getString("_keyExpr", "id");
		String parentIdExpr = row.getString("_parentIdExpr", "parent_id");
		String seqExpr = row.getString("_seqExpr", "seq");
		int movedSeq = row.getInt(seqExpr);
		row.put(keyExpr, ctx.getId());
		
		// update new siblings
		int seq = 1;
		List<OcMap> siblings = select(select(keyExpr + ", " + parentIdExpr)
				.where(parentIdExpr + " = :" + parentIdExpr + " and " + keyExpr + " != :" + keyExpr)
				.addAll(row.sub(parentIdExpr, keyExpr)).orderby(seqExpr)).getData();
		for (OcMap sibling : siblings) {
			if (seq == movedSeq)
				seq++;
			update(sibling.get(keyExpr), m(seqExpr, seq++));
		}

		OcMap old = get(select(keyExpr + ", " + parentIdExpr).id(ctx.id()));
		if (old != null && !old.eq(parentIdExpr, row.get(parentIdExpr))) {
			// update old siblings
			seq = 1;
			siblings = select(select(keyExpr)
					.where(parentIdExpr + " = :" + parentIdExpr + " and " + keyExpr + " != :" + keyExpr)
					.addAll(old.sub(parentIdExpr, keyExpr)).orderby(seqExpr)).getData();
			for (OcMap sibling : siblings) {
				update(sibling.get(keyExpr), m(seqExpr, seq++));
			}
		}
	}
	
}
