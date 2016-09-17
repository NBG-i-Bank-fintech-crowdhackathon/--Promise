var express = require('express');
var myParser = require("body-parser");
var NodeRSA = require('node-rsa');

var app = express();

var users = ["dummy1@host.com", "dummy2@host.com", "dummy3@host.com"]
var dataset = []

app.use(myParser.urlencoded({extended : true}));

app.get('/', function(req, res) {
    res.status(200);
    res.type('text/plain');
    res.send('Server is Up!');
});


app.post('/newTransaction', function(req, res) {
    publicKey = req.body.publicKey;
    transactionMessage = req.body.transactionMessage;
    signature = req.body.signature;
    console.log(publicKey);
    console.log(transactionMessage);
    console.log(signature);

    if (verifySign(publicKey, transactionMessage, signature)) {
        res.json("All Good!");
        console.log("All Good!");
        storeTransaction(transactionMessage);
    } else {
       res.json("Nooooot!");
        console.log("Nooooot!");
    }
});

app.post('/pull', function(req, res) {
    publicKey = req.body.publicKey;
    data = req.body.data;
    signature = req.body.signature;
    console.log(publicKey);
    console.log(data);
    console.log(signature);
    output = [];
    if (!verifySign(publicKey, transactionMessage, signature)) {
        for (var line in dataset){
            jsonContent = JSON.parse(line);
            if (jsonContent.target === data) {
                output.append(line);
            }
        }
        res.json("output");
    } else {
        res.json("Nooooot!");
    }
});

app.post('/quote', function(req, res) {
    console.log("init");
    console.log(req.body.publicKey);
    console.log(req.body.transactionMessage);
    console.log(req.body.signature);
    res.json("answer");
    res.json(verifySign(req.body.publicKey, req.body.transactionMessage, req.body.signature));
});

function verifySign(publicKey, transMessage, signature) {
    var key = new NodeRSA({b:512});
    key.importKey(publicKey, 'pkcs8-public');
    //return key.verify(transMessage, signature);
    return true;
}

function storeTransaction(transactionMessage) {
    var jsonContent = JSON.parse(transactionMessage);
    dataset.push(transactionMessage)
}

function Utf8ArrayToStr(array) {
    var out, i, len, c;
    var char2, char3;

    out = "";
    len = array.length;
    i = 0;
        while(i < len) {
            c = array[i++];
            switch(c >> 4)
            {
              case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                out += String.fromCharCode(c);
                break;
              case 12: case 13:
                // 110x xxxx   10xx xxxx
                char2 = array[i++];
                out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
              case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                char2 = array[i++];
                char3 = array[i++];
                out += String.fromCharCode(((c & 0x0F) << 12) |
                           ((char2 & 0x3F) << 6) |
                           ((char3 & 0x3F) << 0));
                break;
        }
      }

    return out;
}

app.listen(process.env.PORT || 8011);
