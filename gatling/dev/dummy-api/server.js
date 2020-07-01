/**
 * albot の開発・テストに使うダミーの API たち.
 */
const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const responseHeader = {
  'Content-Type': 'application/json',
};

const badRequest = (res) => res.status(400).set(responseHeader).end();

// リクエストボディのパース
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

let lastLineAtReplyCalls = {};

app.post('/reply', (req, res) => {
  console.log(req.body);

  if (!(req.body && req.body.replyToken))
    return res.status(401).json({ message: 'Invalid replyToken' });

  console.log(`reply ${req.body.replyToken}`)

  lastLineAtReplyCalls[req.body.replyToken] = req.body;

  return res.status(200).end();
});

app.get('/reply/last/:id', (req, res) => {
  console.log(`last ${req.params.id}`)
  console.log(`last ${lastLineAtReplyCalls[req.params.id]}`)
  return res.status(200).json(lastLineAtReplyCalls[req.params.id]);
});

app.delete('/reply/last/:id', (req, res) => {
  lastLineAtReplyCalls[req.params.id] = null;
  return res.status(200).end();
});

/**
 * fallback
 */
app.get('/*', (req, res) => {
  console.log(JSON.stringify(req));
  return badRequest(res)
});

app.listen(8989, () => console.log('server started at port 8989...'));
