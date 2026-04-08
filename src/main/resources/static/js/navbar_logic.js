document.addEventListener("DOMContentLoaded", function () {
    const navEl = document.querySelector("nav.navbar");
    if (!navEl) return;

    const isAuthenticated = navEl.dataset.authenticated === "true";
    const role = navEl.dataset.role || "";

    const loginItem = document.getElementById("nav-login-link");
    const userMenuItem = document.getElementById("nav-user-menu");
    const userDropdown = document.getElementById("userDropdown");
    const logoutBtn = document.getElementById("logoutBtn");

    const allNavItems = document.querySelectorAll(".nav-item:not(#nav-user-menu):not(#nav-login-link)");
    const dashboardItem = document.querySelector("a.dropdown-item[href='/dashboard']")?.parentElement;
    const trabajadorItems = document.querySelectorAll(".menu-trabajador");

    if (!isAuthenticated) {
        loginItem?.classList.remove("d-none");
        userMenuItem?.classList.add("d-none");
        allNavItems.forEach((item) => (item.style.display = "block"));
        if (dashboardItem) dashboardItem.style.display = "none";
    } else if (role === "TRABAJADOR") {
        loginItem?.classList.add("d-none");
        userMenuItem?.classList.remove("d-none");

        allNavItems.forEach((item) => {
            const href = item.querySelector("a")?.getAttribute("href");
            item.style.display = href === "/dashboard" ? "block" : "none";
        });

        if (dashboardItem) dashboardItem.style.display = "block";
        if (userDropdown) userDropdown.innerHTML = "Hola, <b>Trabajador</b>";
    } else {
        loginItem?.classList.add("d-none");
        userMenuItem?.classList.remove("d-none");
        allNavItems.forEach((item) => (item.style.display = "block"));
        if (dashboardItem) dashboardItem.style.display = "none";
        if (userDropdown) userDropdown.innerHTML = "Hola, <b>Cliente</b>";
    }

    trabajadorItems.forEach((item) => {
        item.style.display = role === "TRABAJADOR" ? "block" : "none";
    });

    if (logoutBtn) {
        logoutBtn.addEventListener("click", async function (e) {
            e.preventDefault();
            try {
                await fetch("/logout", { method: "POST" });
            } catch (err) {
                console.warn("No se pudo cerrar sesión en el servidor", err);
            }
            location.href = "/";
        });
    }
});

