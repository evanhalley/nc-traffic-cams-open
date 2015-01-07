// imports
var express = require('express'),
	fs = require('fs'),
	https = require('https'),
	winston = require('winston');

// configuration file reading
GLOBAL.config = null;
GLOBAL.logger = null;
var env = process.argv[2];
if(env == 'dev') {
    GLOBAL.config = require('./config/dev.config.js');
} else if(env == 'pro') {
	GLOBAL.config = require('./config/pro.config.js');
} else {
	GLOBAL.config = require('./config/dev.config.js');
}

// configure our logger
var logger = new (winston.Logger)({
  transports: [
    new (winston.transports.Console)({ json: false, timestamp: true, colorize: true, level: config.logger.level }),
    new winston.transports.File({ filename: 'log/camera-api.log', json: false, colorize: true, level: config.logger.level })
  ],
  exceptionHandlers: [
    new (winston.transports.Console)({ json: false, timestamp: true, colorize: true, level: config.logger.level }),
    new winston.transports.File({ filename: 'log/camera-api-fatal.log', json: false, colorize: true, level: config.logger.level })
  ],
  exitOnError: true
});
GLOBAL.logger = logger;

// imports
var cameras = require('./routes/cameras');

// read our ssl certificate
var privateKey = fs.readFileSync('certs/key.pem');
var certificate = fs.readFileSync('certs/cert.pem');
var credentials = { key: privateKey, cert: certificate };

// create our express instance and configure it
var app = express();
app.configure(function () {
	// gzip
	app.use(express.compress());
    app.use(express.bodyParser());
});

if(env == 'pro') {
	app.use(express.basicAuth('nc_traffic_cams', 'tr4ff1c_c4ms!'));
}

// configure out rest calls
app.get('/v1/cameras', cameras.getCameras);
app.get('/v1/cameras/id/:id', cameras.getCameras);
app.get('/v1/cameras/state/:state', cameras.getCameras);
app.get('/v1/cameras/city/:city', cameras.getCameras);
app.get('/v1/latest', cameras.getLatest);

// configure the http and https servers
var httpsServer = https.createServer(credentials, app);
httpsServer.listen(config.server.https.port);
logger.info('Listening on (https) port ' + config.server.https.port + '...');