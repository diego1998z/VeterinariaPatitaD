# Cliente Externo (Demo de Consumo API)

Este cliente demuestra el punto de la rúbrica:

- API creada en Spring Boot: `GET /api/public/estado-clinica`
- Consumo en entorno diferente: página externa `html + js`

## Cómo ejecutar la demo

1. Levanta Spring Boot en `http://localhost:8081`.
2. En otra terminal, entra a `cliente-externo`.
3. Ejecuta un servidor estático:

```powershell
python -m http.server 5500
```

4. Abre `http://localhost:5500`.
5. Pulsa **Consultar API**.
