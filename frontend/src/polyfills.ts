// Polyfill for Node.js 'global' used by sockjs-client and other CJS packages
(window as any).global = window;
