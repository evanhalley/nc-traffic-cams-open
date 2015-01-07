var mongo = require('mongodb');

var Server = mongo.Server,
	Db = mongo.Db,
	BSON = mongo.BSONPure;

var server = new Server(GLOBAL.config.db.server, GLOBAL.config.db.port, 
	{auto_reconnect: true});
db = new Db(GLOBAL.config.db.name, server);

var cameraColl = "camera";
var logger = GLOBAL.logger;

db.open(function(err, db) {
    if(!err) {
       logger.info("Connected to " + GLOBAL.config.db.name + " database");
    }    
});

/* 
 * returns the latest update datetime
 * used to see if the cameras have been updated
 */
exports.getLatest = function(req, res) {
	logger.info("*****Incoming connection from " + req.ip + "*****");
	logger.info("Requested url: " + req.originalUrl);
	logger.info("Getting latest update datetime...");
	logger.profile('getLatest');
	
	db.collection(cameraColl, function(err, collection) {

		collection.find({},{ _id : 0, updated : 1 }).sort( { 'updated' : -1 }).limit(1)
			.toArray(function(err, item) {

			if(!err) {

				if(item && item.length > 0) {
					logger.profile('getLatest');
					res.send(200, item[0]); 
				} else {
					console.log('No item found');
					logger.profile('getLatest');
					res.send(200, new Object());
				}
			} else {
				logger.error("error");
				logger.error(err);
				logger.profile('getLatest');
				res.send(500);
			}
		});
	});
}

/*
 * Gets all cameras or by city or by state
 */
exports.getCameras = function(req, res) {
	logger.info("*****Incoming connection from " + req.ip + "*****");
	logger.info("Requested url: " + req.originalUrl);
	logger.info("Getting traffic cameras...");
	logger.profile('getCameras');

	var query = null;

	var stateParam = req.params.state;

	if(!query && stateParam) {
		query = { "state" : stateParam };
	}

	var cityParam = req.params.city;

	if(!query && cityParam) {
		query = { "city" : cityParam };
	}

	var idParam = req.params.id;

	if(!query && idParam) {
		query = { "_id" : idParam };
	}

	db.collection(cameraColl, function(err, collection) {
		if(query) {
			logger.info("Query: " + JSON.stringify(query));

			collection.find(query).toArray(function(err, items) {

				if(!err) {

					if(items) {
						logger.info("Items found: " + items.length);
						logger.profile('getCameras');
						res.send(200, items); 
					} else {
						logger.info('No items found');
						logger.profile('getCameras');
						res.send(200, new Object());
					}
				} else {
					logger.error("error");
					logger.error(err);
					logger.profile('getCameras');
					res.send(500)
				}
			});
		} else {
			collection.find().toArray(function(err, items) {

				if(!err) {

					if(items) {
						logger.info("Items found: " + items.length);
						logger.profile('getCameras');
						res.send(200, items); 
					} else {
						logger.info('No items found');
						logger.profile('getCameras');
						res.send(200, new Object());
					}
				} else {
					logger.error("error");
					logger.error(err);
					logger.profile('getCameras');
					res.send(500)
				}
			});
		}
	});
}