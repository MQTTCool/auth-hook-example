module.exports = {
    "env": {
        "browser": true,
        "es6": true,
        "amd": true
    },
    "extends": "eslint:recommended",
    "rules": {
        "indent": [
            "error",
            2,
            {"SwitchCase": 1}
        ],
        "no-trailing-spaces": [
            "error"
        ],
        "max-len": [
            "error",
            80]
        ,
        "no-spaced-func": [
            "error"
        ],
        "linebreak-style": [
            "error",
            "unix"
        ],
        "quotes": [
            "error",
            "single"
        ],
        "semi": [
            "error",
            "always"
        ],
        "no-unused-vars": [
            "warn"
        ],
        "valid-jsdoc": [
            "warn", {
                "requireParamDescription": false,
                "requireReturnDescription": false,
                "requireReturn": false,
                "requireReturnType": true
            }
        ]
    }
};