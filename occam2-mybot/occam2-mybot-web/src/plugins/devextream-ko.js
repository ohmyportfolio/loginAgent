import config from 'devextreme/core/config';
import { locale, loadMessages } from "devextreme/localization";
import koMessages from "./devextream-ko.json";

config({ defaultCurrency: 'KRW', forceIsoDateParsing: true }); 
loadMessages(koMessages);
locale("ko");