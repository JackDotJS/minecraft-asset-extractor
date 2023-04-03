import { defineConfig } from 'vite';
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  base: `/minecraft-asset-extractor/`,
  publicDir: 'assets',
  plugins: [
    VitePWA({
      injectRegister: 'auto',
      manifest: {
        "name": "Minecraft Asset Extractor",
        "short_name": "MC Extractor",
        "start_url": ".",
        "display": "standalone",
        "background_color": "#f48b2f",
        "description": "A web app that aims to simplify the process of extracting Minecraft's built-in game assets, including things like textures, sounds, models, JSON data, and more. This app works entirely through your browser.",
        "icons": [
          {
            "src": "icon144x.png",
            "sizes": "144x144",
            "type": "image/png"
          }
        ]
      }
    })
  ]
});