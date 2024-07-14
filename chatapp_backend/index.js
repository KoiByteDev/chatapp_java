import express from "express";
import { initializeApp } from "firebase/app";
import { getFirestore, collection, addDoc, getDocs, getDoc, query, where, doc, updateDoc, arrayUnion, setDoc } from "firebase/firestore";
import { WebSocketServer } from "ws";
import http from "http";
import bcrypt from "bcrypt";

const firebaseConfig = {
  apiKey: "AIzaSyBJwgYEjx6Ch4Xu_LPqoJlnk393fvHxUZA",
  authDomain: "chatapp-f2a19.firebaseapp.com",
  databaseURL: "https://chatapp-f2a19-default-rtdb.firebaseio.com",
  projectId: "chatapp-f2a19",
  storageBucket: "chatapp-f2a19.appspot.com",
  messagingSenderId: "714799579263",
  appId: "1:714799579263:web:4413560fdd8f8404ba3f4a",
};

const firebaseApp = initializeApp(firebaseConfig);
const db = getFirestore(firebaseApp);

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

app.get("/", (req, res) => {
  res.send("WebSocket server is running");
});

wss.on("connection", (ws) => {
  let currentUser = null;

  ws.on("message", async (message) => {
    try {
      message = message.toString();
      console.log(`Received message: ${message}`);
      const [action, username, password, email] = message.split(",");
      let user, q, querySnapshot;

      switch (action) {
        case "register":
          q = query(collection(db, "users"), where("username", "==", username));
          querySnapshot = await getDocs(q);

          querySnapshot.forEach((doc) => {
            user = doc.data();
          });

          if (user) {
            ws.send("El nombre de usuario ya esta en uso.");
          } else {
            const hashedPassword = await bcrypt.hash(password, 10);
            await addDoc(collection(db, "users"), {
              username,
              email,
              password: hashedPassword,
            });
            ws.send("Registro exitoso!");
          }
          
          break;

        case "login":
          q = query(collection(db, "users"), where("username", "==", username));
          querySnapshot = await getDocs(q);
          querySnapshot.forEach((doc) => {
            user = doc.data();
          });
          

          if (user && (await bcrypt.compare(password, user.password))) {
            ws.send("Inicio de sesión exitoso!");
          } else {
            ws.send("Usuario o contraseña incorrectos");
          }
          break;

        case "join":
          currentUser = username;
          const messagesSnapshot = await getDocs(collection(db, "messages"));
          let messages = "";

          messagesSnapshot.forEach((doc) => {
            const messageArray = doc.data().messages;
            messageArray.forEach((msg) => {
              messages +=`${msg.time} - ${msg.message}&`;
            });
          });

          ws.send(messages, (error) => {
            if (error) {
                console.error("Error sending message:", error);
            } else {
                console.log("Message sent successfully.");
            }

          });

          broadcast(`${username} se ha unido a la conversación`);
          break;

        case "leave":
          broadcast(`${username} ha abandonado la conversación`);
          break;

        case "msg":
          const msg = message.split(',')[1];
          const currentTime = new Date();
          const dateString = currentTime.toLocaleDateString('en-US').split("/").join("-");
          const timeString = currentTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
          
          const messageObject = { message: msg, time: timeString };

          const messageDocRef = doc(db, "messages", dateString);
          const messageDoc = await getDoc(messageDocRef);

          if (messageDoc.exists()) {
            await updateDoc(messageDocRef, {
              messages: arrayUnion(messageObject)
            });
          } else {
            await setDoc(messageDocRef, {
              messages: [messageObject]
            });
          }

          broadcast(messageObject.time + " - " + messageObject.message);
          break;
        case "searchForFrendPls🥺":
          const searchTerm = message.split(',')[1];
          q = query(collection(db, "users"), where("username", ">=", searchTerm), where("username", "<=", searchTerm + "\uf8ff"));
          querySnapshot = await getDocs(q);
          const foundUsers = [];
          querySnapshot.forEach((doc) => {
            foundUsers.push(doc.data().username);
          });
          ws.send("foundFriends😸:" + foundUsers.join(","));
          break;

      }
    } catch (err) {
      console.log(err.message);
    }
  });

  ws.on("close", () => {
    if (currentUser) {
      broadcast(`${currentUser} ha abandonado la conversación`);
    }
    console.log("Client disconnected");
  });
});

const broadcast = (message) => {
  wss.clients.forEach((client) => {
    if (client.readyState === client.OPEN) {
      client.send(message);
    }
  });
};

const PORT = process.env.PORT || 8080;
server.listen(PORT, () => {
  console.log(`El servidor está abierto en el puerto ${PORT}`);
});
