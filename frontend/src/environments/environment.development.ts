export const environment = {
  production: false,
  api: {
    // Dev: SPA na :4200 woła backend cross-origin (CORS + withCredentials),
    // dokładnie jak w debtor/hercu-pulpit — bez proxy.
    baseUrl: 'http://localhost:8080',
  },
};
