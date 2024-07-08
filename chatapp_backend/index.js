import express from 'express';
import { WebSocketServer } from 'ws';
import http from 'http';
import bcrypt from 'bcrypt';

const app = express();

const users = [];
const messages = [];

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
        users.push({ username, email, password: hashedPassword });
        ws.send('Registro exitoso!');
      } else if (action === 'login') {
        const user = users.find(u => u.username === username);
        if (user && await bcrypt.compare(password, user.password)) {
          ws.send('Inicio de sesión exitoso!');
        } else {
          ws.send('Usuario o contraseña incorrectos');
        }
      } else if (action === 'join') {
        currentUser = username;
        ws.send(messages.join('\n'));
        broadcast(`${username} se ha unido a la conversación`);
      } else if (action === 'leave') {
        broadcast(`${username} ha abandonado la conversación`);
      } else {
        messages.push(message);
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
