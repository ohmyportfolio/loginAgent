document.getElementById('send').addEventListener('click', () => {
    chrome.tabs.query({active: true, currentWindow: true}, (tabs) => {
        var port = chrome.runtime.connectNative('com.happy.loginAgent');
        port.postMessage({url: tabs[0].url});
        port.onMessage.addListener(function(msg) {
            console.log("Received" + msg);
        });
    });
});
