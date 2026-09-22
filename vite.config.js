import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 9001, // giữ giống port dev của taiga-front (npm start -> 9001) cho quen
  },
});
