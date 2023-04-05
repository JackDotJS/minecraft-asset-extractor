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
        "background_color": "#9E512F",
        "theme_color": "#9E512F",
        "description": "A web app that aims to simplify the process of extracting Minecraft's built-in game assets, including things like textures, sounds, models, JSON data, and more. This app works entirely through your browser.",
        "icons": [
          {
            "src": "icon48x.png",
            "sizes": "48x48",
            "type": "image/png"
          },
          {
            "src": "icon72x.png",
            "sizes": "72x72",
            "type": "image/png"
          },
          {
            "src": "icon96x.png",
            "sizes": "96x96",
            "type": "image/png"
          },
          {
            "src": "icon144x.png",
            "sizes": "144x144",
            "type": "image/png"
          },
          {
            "src": "icon168x.png",
            "sizes": "168x168",
            "type": "image/png"
          },
          {
            "src": "icon192x.png",
            "sizes": "192x192",
            "type": "image/png"
          },
          {
            "src": "icon256x.png",
            "sizes": "256x256",
            "type": "image/png"
          },
          {
            "src": "icon512x.png",
            "sizes": "512x512",
            "type": "image/png"
          }
        ]
      }
    })
  ]
});