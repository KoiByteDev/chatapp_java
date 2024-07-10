import express from 'express';
import { initializeApp } from "firebase/app";
import { getFirestore, collection, addDoc, getDocs, query, where } from "firebase/firestore";
import { WebSocketServer } from 'ws';
import http from 'http';
import bcrypt from 'bcrypt';

const firebaseConfig = {
  apiKey: "AIzaSyBJwgYEjx6Ch4Xu_LPqoJlnk393fvHxUZA",
  authDomain: "chatapp-f2a19.firebaseapp.com",
  databaseURL: "https://chatapp-f2a19-default-rtdb.firebaseio.com",
  projectId: "chatapp-f2a19",
  storageBucket: "chatapp-f2a19.appspot.com",
  messagingSenderId: "714799579263",
  appId: "1:714799579263:web:4413560fdd8f8404ba3f4a"
};

const firebaseApp = initializeApp(firebaseConfig);
const db = getFirestore(firebaseApp);

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

app.get('/', (req, res) => {
  res.send('WebSocket server is running');
});

wss.on('connection', (ws) => {
  let currentUser = null;

  ws.on('message', async (message) => {
    try {
      message = message.toString();
      console.log(`Received message: ${message}`);
      const [action, username, password, email] = message.split(',');

      if (action === 'register') {
        const hashedPassword = await bcrypt.hash(password, 10);
        await addDoc(collection(db, 'users'), { username, email, password: hashedPassword });
        ws.send('Registro exitoso!');
      } else if (action === 'login') {
        const q = query(collection(db, 'users'), where('username', '==', username));
        const querySnapshot = await getDocs(q);
        let user = null;
        querySnapshot.forEach(doc => {
          user = doc.data();
        });

        if (user && await bcrypt.compare(password, user.password)) {
          ws.send('Inicio de sesión exitoso!');
        } else {
          ws.send('Usuario o contraseña incorrectos');
        }
      } else if (action === 'join') {
        currentUser = username;
        const messagesSnapshot = await getDocs(collection(db, 'messages'));
        let messages = '';
        messagesSnapshot.forEach(doc => {
          messages += doc.data().message + '\n';
        });
        ws.send(messages);
        broadcast(`${username} se ha unido a la conversación`);
      } else if (action === 'leave') {
        broadcast(`${username} ha abandonado la conversación`);
      } else {
        await addDoc(collection(db, 'messages'), { message });
        broadcast(message);
      }
    } catch (err) {
      console.log(err.message);
    }
  });

  ws.on('close', () => {
    if (currentUser) {
      broadcast(`${currentUser} ha abandonado la conversación`);
    }
    console.log('Client disconnected');
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
  console.log(`Server is listening on port ${PORT}`);
});