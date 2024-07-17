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

const clients = new Map();

app.get("/", (req, res) => {
  res.send("WebSocket server is running");
});

wss.on("connection", (ws) => {
  let currentUser = null;

  ws.on("message", async (message) => {
    try {
      message = message.toString();
      console.log(`Received message: ${message}`);
      const [...params] = message.split(",");
      const action = params[0];
      let user, q, querySnapshot;

      switch (action) {
        case "register":
          const [, username, password, email] = params;
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
              friends: []
            });
            ws.send("Registro exitoso!");
          }
          break;

        case "login":
          const [, loginUsername, loginPassword] = params;
          q = query(collection(db, "users"), where("username", "==", loginUsername));
          querySnapshot = await getDocs(q);
          querySnapshot.forEach((doc) => {
            user = { ...doc.data(), id: doc.id };
          });

          if (user && (await bcrypt.compare(loginPassword, user.password))) {
            currentUser = loginUsername;
            clients.set(loginUsername, ws);
            ws.send("Inicio de sesión exitoso!");
            const friendList = user.friends || [];
            ws.send("friendList," + friendList.join(","));
          } else {
            ws.send("Usuario o contraseña incorrectos");
          }
          break;

        case "join":
          const [, joinUsername] = params;
          currentUser = joinUsername;
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

          broadcast(`${joinUsername} se ha unido a la conversación`);
          break;

        case "leave":
          const [, leaveUsername] = params;
          broadcast(`${leaveUsername} ha abandonado la conversación`);
          break;

        case "privateMsg":
          const [, sender, recipient, privateMsg] = params;
          console.log(params);
          if (recipient) {
            const currentTime = new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
            const formattedMsg = `${currentTime} - ${sender}: ${privateMsg}`;
            
            const chatId = [sender, recipient].sort().join(" to ");
            const privatesChatRef = doc(db, "privates", chatId);
            const privatesChatDoc = await getDoc(privatesChatRef);

            if (privatesChatDoc.exists()) {
              await updateDoc(privatesChatRef, {
                messages: arrayUnion({
                  sender,
                  message: privateMsg,
                  time: currentTime
                })
              });
            } else {
              await setDoc(privatesChatRef, {
                messages: [{
                  sender,
                  message: privateMsg,
                  time: currentTime
                }]
              });
            }

            ws.send(`privateMsg,${recipient},${formattedMsg}`);
          }
          break;

        case "msg":
          const [, msg] = params;
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
          const [, searchTerm] = params;
          q = query(collection(db, "users"), where("username", ">=", searchTerm), where("username", "<=", searchTerm + "\uf8ff"));
          querySnapshot = await getDocs(q);
          const foundUsers = [];
          querySnapshot.forEach((doc) => {
            foundUsers.push(doc.data().username);
          });
          ws.send("foundFriends😸:" + foundUsers.join(","));
          break;

        case "addFriend":
          const [, currentUsername, friendUsername] = params;
          const currentUserDoc = await getDocs(query(collection(db, "users"), where("username", "==", currentUsername)));
          const friendUserDoc = await getDocs(query(collection(db, "users"), where("username", "==", friendUsername)));

          if (!currentUserDoc.empty && !friendUserDoc.empty) {
            const currentUser = currentUserDoc.docs[0];
            const friendUser = friendUserDoc.docs[0];

            await updateDoc(currentUser.ref, {
              friends: arrayUnion(friendUsername)
            });

            const updatedUser = await getDoc(currentUser.ref);
            const updatedFriendList = updatedUser.data().friends || [];
            ws.send("friendList," + updatedFriendList.join(","));
          } else {
            ws.send("User not found");
          }
          break;
        case "fetchFriendMessages":
          const [, us, rec] = params;
          console.log(us, rec)
          const chatId = [us, rec].sort().join(" to ");
          const privateChatRef = doc(db, "privates", chatId);
          const privateChatDoc = await getDoc(privateChatRef);
          let privMsgs = "";

          if (privateChatDoc.exists()) {
            const messageArray = privateChatDoc.data().messages;
            messageArray.forEach((msg) => {
              privMsgs += `${msg.time} - ${msg.sender}: ${msg.message}&`;
            });
          }

          ws.send(`messagesFor,${rec},${privMsgs}`);
          break;

        case "fetchFriendList":
          const [, fetchingUser] = params;
          const userQuery = query(collection(db, "users"), where("username", "==", fetchingUser));
          const userQuerySnapshot = await getDocs(userQuery);
          
          if (!userQuerySnapshot.empty) {
            const userDoc = userQuerySnapshot.docs[0];
            const userData = userDoc.data();
            const friendList = userData.friends || [];
            ws.send("friendList," + friendList.join(","));
          } else {
            ws.send("User not found");
          }
          break;
          case "fetchMessages":
            try {
              const esteEsUnNombreDeVariableComicamenteLargoQueMuyProbablementeNadieLoVayaALeer_SeSuponeQueEsLaVariableQueContieneLosMensajesjejejejejejejejejeejjejejejejeej = await getDocs(collection(db, "messages"));
              let messages = "";
          
              esteEsUnNombreDeVariableComicamenteLargoQueMuyProbablementeNadieLoVayaALeer_SeSuponeQueEsLaVariableQueContieneLosMensajesjejejejejejejejejeejjejejejejeej.forEach((doc) => {
                const messageArray = doc.data().messages;
                if (Array.isArray(messageArray)) {
                  messageArray.forEach((msg) => {
                    if (msg.time && msg.message) {
                      messages += `${msg.time} - ${msg.message}&`;
                    }
                  });
                }
              });
          
              if (messages) {
                ws.send("allMessages," + messages, (error) => {
                  if (error) {
                    console.error("Error sending messages:", error);
                  } else {
                    console.log("Messages sent successfully.");
                  }
                });
              } else {
                ws.send("allMessages,No messages found");
              }
            } catch (error) {
              console.error("Error fetching messages:", error);
              ws.send("error,Failed to fetch messages");
            }
            break;

      }
    } catch (err) {
      console.log(err.message);
    }
  });

  ws.on("close", () => {
    if (currentUser) {
      clients.delete(currentUser);
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