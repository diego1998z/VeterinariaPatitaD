const API_URL = "http://localhost:8081/api/public/estado-clinica";

const btnCargar = document.getElementById("btnCargar");
const statusEl = document.getElementById("status");
const resultadoEl = document.getElementById("resultado");

const fields = {
    nombreClinica: document.getElementById("nombreClinica"),
    direccion: document.getElementById("direccion"),
    telefono: document.getElementById("telefono"),
    horario: document.getElementById("horario"),
    atencionActiva: document.getElementById("atencionActiva"),
    medicosRegistrados: document.getElementById("medicosRegistrados"),
    fechaServidor: document.getElementById("fechaServidor"),
    horaServidor: document.getElementById("horaServidor"),
};

async function consultarEstadoClinica() {
    statusEl.textContent = "Consultando API...";
    resultadoEl.classList.add("hidden");

    try {
        const response = await fetch(API_URL);
        const payload = await response.json();

        if (!response.ok || !payload.success || !payload.data) {
            throw new Error(payload.message || "No se pudo obtener respuesta válida.");
        }

        const data = payload.data;
        fields.nombreClinica.textContent = data.nombreClinica || "-";
        fields.direccion.textContent = data.direccion || "-";
        fields.telefono.textContent = data.telefono || "-";
        fields.horario.textContent = data.horario || "-";
        fields.atencionActiva.textContent = data.atencionActiva ? "Sí" : "No";
        fields.medicosRegistrados.textContent = data.medicosRegistrados ?? "-";
        fields.fechaServidor.textContent = data.fechaServidor || "-";
        fields.horaServidor.textContent = data.horaServidor || "-";

        statusEl.textContent = "Consulta completada.";
        resultadoEl.classList.remove("hidden");
    } catch (error) {
        statusEl.textContent = "Error: " + error.message;
    }
}

btnCargar.addEventListener("click", consultarEstadoClinica);
