const functions = require("firebase-functions");
var admin = require("firebase-admin");

var serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://keystore-48170-default-rtdb.europe-west1.firebasedatabase.app"
});

exports.androidPushNotification = functions.firestore.document("Products/{docId}").onCreate(
    (snapshot, context) => {
        const product = snapshot.data();

        const message = {
            topic: "products",
            notification: {
                title: "New game",
                body: `The game ${product.name} is now available!`
            },
            data: {
                productId: context.params.docId,
                productName: product.name
            }
        };

        admin.messaging().send(message)
            .then((response) => {
                console.log("Successfully sent message:", response);
            })
            .catch((error) => {
                console.error("Error sending message:", error);
            });
    }
);
