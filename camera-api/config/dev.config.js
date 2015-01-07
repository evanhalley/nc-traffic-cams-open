var config = {};
config.server = {};
config.server.https = {};
config.db = {};
config.logger = {};

config.server.https.port = 8443;

config.db.server = 'localhost';
config.db.port = 27017;
config.db.name = 'nc-traffic-cams';

config.logger.level = 'debug';

module.exports = config;