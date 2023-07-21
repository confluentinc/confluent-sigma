// Constants.js

const prod = {
    URL: {
        SERVER_ENDPOINT: '/'
    }
};
   
const dev = {
    URL: {
        SERVER_ENDPOINT: 'http://localhost:8080/',
    }
};
   
export const CONFIG = process.env.NODE_ENV === 'development' ? dev : prod;