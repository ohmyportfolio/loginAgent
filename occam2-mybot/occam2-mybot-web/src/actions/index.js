import downloadLabel from "./downloadLabel";
import auth from "../../../../occam2-base/occam2-base-web/src/services/auth";
import appInfo from "../app-info";

export default {
    downloadLabel,
    isSafetyDept() {
        for (const safetyDept of appInfo.safetyDepts) {
            if(auth.user.all_group_ids && auth.user.all_group_ids.indexOf(safetyDept) > -1) {
                return true;
            }
        }
        return false
    }
}