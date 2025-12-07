
# Este script construye y levanta los contenedores de Docker para la aplicación.
Write-Host "Levantando los contenedores de la aplicación con Docker Compose en modo detached..."
docker-compose up --build -d
