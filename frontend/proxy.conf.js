// Dev server proxy: the app calls /api on its own origin, like it does behind nginx in production.
// API_URL is set by docker-compose.yml; outside Docker the backend runs on localhost:8080.
module.exports = {
  '/api': {
    target: process.env.API_URL || 'http://localhost:8080',
    secure: false,
    changeOrigin: false,
  },
};
