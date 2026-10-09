# Etapa 1: compilar con el SDK
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Primero solo proyectos/manifests: el restore queda cacheado si no cambian.
COPY RfidPlatform.sln global.json ./
COPY RFID.Domain/RFID.Domain.csproj RFID.Domain/
COPY RFID.Application/RFID.Application.csproj RFID.Application/
COPY RFID.Infrastructure/RFID.Infrastructure.csproj RFID.Infrastructure/
COPY RFID.Api/RFID.Api.csproj RFID.Api/
RUN dotnet restore RfidPlatform.sln

COPY . .
RUN dotnet publish RFID.Api/RFID.Api.csproj -c Release -o /app/publish --no-restore /p:UseAppHost=false

# Etapa 2: solo la imagen runtime (~220 MB) con el publish
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .

# El contenedor escucha en 8080 (puerto por defecto de las imágenes .NET 8);
# el compose lo publica en el 5000 que ya usa la app Android.
EXPOSE 8080
ENTRYPOINT ["dotnet", "RFID.Api.dll"]
