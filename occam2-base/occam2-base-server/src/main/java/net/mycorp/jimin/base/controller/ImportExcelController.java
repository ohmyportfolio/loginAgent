package net.mycorp.jimin.base.controller;

import static net.mycorp.jimin.base.core.Global.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.mycorp.jimin.base.domain.OcMap;
import net.mycorp.jimin.base.domain.OcResult;
import net.mycorp.jimin.base.util.ServletHelper;

@Controller
@Transactional
@RequestMapping(value = "api")
public class ImportExcelController {
	protected static Logger log = LoggerFactory.getLogger(ImportExcelController.class);

	private static String CELL_DATA_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	private static String CELL_NUMBERIC_FORMAT = "#.##";

	@RequestMapping(path = "import/excel", method = RequestMethod.POST)
	@ResponseBody
	public void upload(@RequestParam("files[]") MultipartFile[] files, @RequestParam Map<String, Object> attach)
			throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		String resource = (String) attach.get("resource");
		List<OcMap> fields = Arrays.asList(objectMapper.readValue((String) attach.get("fields"), OcMap[].class));

		OcMap defaultData = new OcMap();
		if (attach.containsKey("defaultData")) {
			defaultData = objectMapper.readValue((String) attach.get("defaultData"), OcMap.class);
		}

		InputStream inputStream = new BufferedInputStream(files[0].getInputStream());
		// TODO 엑셀 파일 읽다가 오류나는 경우 사용자에게 에러메시지 출력
		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
		boolean hasFailed = false;
		try {
			List<OcMap> datas = getDatasFromExcel(workbook, fields);
			XSSFSheet sheet = workbook.getSheetAt(0);

			for (int i = datas.size() - 1; i >= 0; i--) {
				OcMap data = datas.get(i);
				try {
					Boolean isFailed = (Boolean) data.get("isRowFailed");
					if (!isFailed) {
						OcMap row = (OcMap) defaultData.clone();
						row.putAll(data);
						Number result = (Number) command("update").resource(resource).id(row.get("id")).row(row).skipPermit(true).execute();
						if(result.longValue() == 0)
							command("insert").resource(resource).row(row).execute();
						removeRow(sheet, i + 1);
					} else {
						hasFailed = true;
					}
				} catch (Exception e) {
					XSSFCell cell = sheet.getRow(i + 1).getCell(0);
					setCellComment(cell, e.getMessage());
					log.error("Exception stacktrace: ", e);
					data.add("isRowFiled", true);
					hasFailed = true;
				}
			}

			if(!hasFailed)
				return;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			byte[] bytes = outputStream.toByteArray();
			InputStream is = new ByteArrayInputStream(bytes);
			ServletHelper.download(is, Long.valueOf(bytes.length), "failedList.xlsx",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true);
		} finally {
			workbook.close();
		}
	}

	public List<OcMap> getDatasFromExcel(XSSFWorkbook workbook, List<OcMap> fields) {
		List<OcMap> datas = new ArrayList<OcMap>();

		if (workbook.getNumberOfSheets() <= 0) {
			return null;
		}

		XSSFSheet sheet = workbook.getSheetAt(0);
		XSSFRow row;
		XSSFCell cell;
		List<OcMap> matchedFields = new ArrayList<OcMap>();
		int headerSize = 0;

		for (int rowIndex = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
			OcMap data = new OcMap();
			row = sheet.getRow(rowIndex);
			Boolean isFailed = false;

			if (headerSize == 0)
				headerSize = row.getPhysicalNumberOfCells();

			for (int cellIndex = 0; cellIndex < headerSize; cellIndex++) {
				cell = row.getCell(cellIndex);

				String value = getCellValue(cell);

				if (rowIndex == 0) {
					for (OcMap field : fields) {
						if (field.get("caption") != null && field.get("caption").equals(value)) {
							matchedFields.add((OcMap) field.clone());
							break;
						}
					}
				} else {
					if (matchedFields.size() <= cellIndex) {
						setCellComment(cell, "엑셀 파일 양식이 일치하지 않습니다.");
						data.add("isRowFailed", true);
						datas.add(data);
						return datas;
					}
					OcMap field = matchedFields.get(cellIndex);
					String dataField = (String) field.get("dataField");

					if (dataField.split("__").length != 1 || dataField.equals("reg_date")
							|| dataField.equals("reg_user__name") || dataField.equals("mod_date")
							|| dataField.equals("mod_user__name")) {
						continue;
					}

					if (StringUtils.isBlank(value) != true) {
						if (field.containsKey("targetType") && field.get("targetType").equals("resource")) {
							String displayField = field.getString("displayField");
							String lastDisplayField = "name";
							if(displayField != null) {
								String[] displayFields = displayField.split("__");
								lastDisplayField = displayFields[displayFields.length - 1];
							}
							OcResult context = (OcResult) command("select").resource((String) field.get("typeId"))
									.condition(lastDisplayField, value.trim()).execute();
							if (context.getTotal() >= 1) {
								List<OcMap> type = (List<OcMap>) context.getData();
								value = (String) type.get(0).get("id");
							} else {
								isFailed = true;
								setCellComment(cell, "일치하는 값이 없습니다. 정확한 값을 입력하세요.");
							}
						} else if (field.containsKey("targetType") && field.get("targetType").equals("code")) {
							String typeId = (String) field.get("typeId");
							OcResult context = (OcResult) command("select").resource("codes")
									.condition(m(p("type_id", typeId), p("or", m("name", value.trim(), "code", value.trim())))).execute();
							if (context.getTotal() >= 1) {
								List<OcMap> type = (List<OcMap>) context.getData();
								value = (String) type.get(0).get("code");
							} else {
								isFailed = true;
								setCellComment(cell, "일치하는 값이 없습니다. 정확한 값을 입력하세요.");
							}
						}
					}
					data.add((String) field.get("dataField"), value);
				}
			}
			if (data.size() != 0) {
				data.add("isRowFailed", isFailed);
				datas.add(data);
			}
		}
		return datas;
	}

	public static void removeRow(XSSFSheet sheet, int rowIndex) {
		int lastRowNum = sheet.getLastRowNum();
		if (rowIndex >= 0 && rowIndex < lastRowNum) {
			sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
		}
		if (rowIndex == lastRowNum) {
			XSSFRow removingRow = sheet.getRow(rowIndex);
			if (removingRow != null) {
				sheet.removeRow(removingRow);
			}
		}
	}

	public static String getCellValue(XSSFCell cell) {
		final DataFormatter df = new DataFormatter();
		String value = df.formatCellValue(cell);

		if (!StringUtils.isBlank(value)) {
			switch (cell.getCellTypeEnum()) {
			case NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					Date date = cell.getDateCellValue();
					value = new SimpleDateFormat(CELL_DATA_FORMAT).format(date);
				} else {
					DecimalFormat decimalFormat = new DecimalFormat(CELL_NUMBERIC_FORMAT);
					value = decimalFormat.format(cell.getNumericCellValue());
				}
				break;
			case STRING:
				value = cell.getStringCellValue();
				break;
			case BOOLEAN:
				value = cell.getBooleanCellValue() + "";
				break;
			default:
				value = new String();
				break;
			}
		}
		return value;
	}

	public static void setCellComment(XSSFCell cell, String message) {
		XSSFCreationHelper factory = cell.getSheet().getWorkbook().getCreationHelper();
		XSSFComment comment = cell.getCellComment();

		if (comment == null) {
			XSSFDrawing drawing = cell.getSheet().createDrawingPatriarch();
			XSSFClientAnchor anchor = factory.createClientAnchor();

			anchor.setCol1(cell.getColumnIndex());
			anchor.setCol2(cell.getColumnIndex() + 1);
			anchor.setRow1(cell.getRowIndex());
			anchor.setRow2(cell.getRowIndex() + 1);
			anchor.setDx1(100);
			anchor.setDx2(1000);
			anchor.setDy1(200);
			anchor.setDy2(2000);
			comment = drawing.createCellComment(anchor);
		}

		XSSFRichTextString str = factory.createRichTextString(message);
		comment.setString(str);
		comment.setAuthor("SYSTEM");
		cell.setCellComment(comment);
	}
}
