export default {
    text: "라벨지 출력", showText: "always", icon: "tags", disabled: true,
    checkEnabled: (selections) => { return selections.length > 0 },
    onClick: (selections, component) => {
        component.util.downloadPost("/api/assets/any/downloadLabel?template=QR_CODE.docx", selections, "labels.docx");
    }
}