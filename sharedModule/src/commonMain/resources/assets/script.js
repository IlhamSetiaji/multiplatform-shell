function callJS() {
    return 'Response from JS';
}

function callNative() {
    window.kmpJsBridge.callNative("Greet",JSON.stringify({message: "Hello"}),
            function (data) {
                document.getElementById("subtitle").innerText = data;
                console.log("Greet from Native: " + data);
            }
        );
}

function callAndroid() {
    window.androidJsBridge.call('1', 'callAndroid', '{"name":"callAndroid"}');
}

function callIOS() {
    window.webkit.messageHandlers.iosJsBridge.postMessage("{\"id\":\"1\",\"methodName\":\"callIOS\",\"params\":\"{\\\"type\\\":\\\"1\\\"}\"}");
}

function callDesktop() {
    window.cefQuery({
            request: "{\"id\":\"1\",\"methodName\":\"callIOS\",\"params\":\"{\\\"type\\\":\\\"1\\\"}\"}",
            onSuccess: function(response) {
            },
            onFailure: function(errorCode, errorMessage) {
            }
        });
}