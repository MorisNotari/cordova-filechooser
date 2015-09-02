module.exports = {
    pick: function (success, failure) {
        cordova.exec(success, failure, "FileChooser", "pick", []);
    }
};
