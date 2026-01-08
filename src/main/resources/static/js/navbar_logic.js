document.addEventListener('DOMContentLoaded', function () {

    // define los modeles
    const mainModalEl = document.getElementById('mainLoginModal');
    const clienteModalEl = document.getElementById('clienteLoginModal');
    const trabajadorModalEl = document.getElementById('trabajadorLoginModal');

    const mainModal = mainModalEl ? new bootstrap.Modal(mainModalEl) : null;
    const clienteModal = clienteModalEl ? new bootstrap.Modal(clienteModalEl) : null;
    const trabajadorModal = trabajadorModalEl ? new bootstrap.Modal(trabajadorModalEl) : null;

    // los elementos del navbar para login/logout y menu del usuario
    const loginLink = document.getElementById('nav-login-link');
    const userMenu = document.getElementById('nav-user-menu');
    const logoutBtn = document.getElementById('logoutBtn');

    // abre el modal y muestra las opciones de rol
    if (loginLink) {
        loginLink.addEventListener('click', function (e) {
            e.preventDefault();
            if (mainModal) mainModal.show();
        });
    }

    // muestra los botones para elegir rol
    const btnCliente = document.getElementById('btnSoyCliente');
    const btnTrabajador = document.getElementById('btnSoyTrabajador');
//si elige cliente esconde el model y muestra el modal de cliente
    if (btnCliente) {
        btnCliente.addEventListener('click', () => {
            if (mainModal) mainModal.hide();
            if (clienteModal) clienteModal.show();
        });
    }
//si es trabajador esconde el principal y muestra el de trabajador
    if (btnTrabajador) {
        btnTrabajador.addEventListener('click', () => {
            if (mainModal) mainModal.hide();
            if (trabajadorModal) trabajadorModal.show();
        });
    }

    // modales de registro y login del cliente
    const showRegisterLink = document.getElementById('showRegisterLink');
    const showLoginLink = document.getElementById('showLoginLink');
    const clienteLoginView = document.getElementById('clienteLoginView');
    const clienteRegisterView = document.getElementById('clienteRegisterView');
    const clienteModalTitle = clienteModalEl ? clienteModalEl.querySelector('.modal-title') : null;

    if (showRegisterLink && showLoginLink && clienteLoginView && clienteRegisterView) {//verifica la existencia de todos

        showRegisterLink.addEventListener('click', (e) => {//si da click en registrar
            e.preventDefault();
            clienteLoginView.style.display = 'none';
            clienteRegisterView.style.display = 'block';
            clienteModalTitle.innerText = 'Registro de Cliente';
        });

        showLoginLink.addEventListener('click', (e) => {//si de click en login
            e.preventDefault();
            clienteLoginView.style.display = 'block';
            clienteRegisterView.style.display = 'none';
            clienteModalTitle.innerText = 'Acceso Cliente';
        });
    }
//intenta inciar sesison
   //manejo de envio de credenciales api y jwt
    async function handleLogin(usernameId, passwordId, errorId, modalInstance) {
        const user = document.getElementById(usernameId).value;
        const pass = document.getElementById(passwordId).value;
        const errorDisplay = document.getElementById(errorId);
//envia las creadenciales al endpoint
        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: user, password: pass })//sigue el formato de payload
            });

            const data = await res.json();

            if (!res.ok) {//si no es ok muestra un error
                errorDisplay.innerText = data.error || 'Credenciales incorrectas.';
                errorDisplay.style.display = 'block';
                return;
            }

            // Guardar JWT y token del usuario
            localStorage.setItem('jwtToken', data.token);
            localStorage.setItem('jwtRole', data.role);
            localStorage.setItem('jwtUser', data.username);

            modalInstance.hide();//esconde el modal y muestra cambios en la interfaz
            location.reload();

        } catch (err) {
            console.error(err);//maneja errores de conexion
            errorDisplay.innerText = 'Error de conexión con el servidor.';
            errorDisplay.style.display = 'block';
        }
    }

    // conecta a los formularios de cliente y trabajador
    if (document.getElementById('clienteLoginForm')) {
        document.getElementById('clienteLoginForm').addEventListener('submit', function (e) {
            e.preventDefault();
            handleLogin('clienteUsername', 'clientePassword', 'clienteLoginError', clienteModal);
        });
    }

    if (document.getElementById('trabajadorLoginForm')) {
        document.getElementById('trabajadorLoginForm').addEventListener('submit', function (e) {
            e.preventDefault();
            handleLogin('trabajadorUsername', 'trabajadorPassword', 'trabajadorLoginError', trabajadorModal);
        });
    }

    //registro de cliente
    if (document.getElementById('clienteRegisterForm')) {

        document.getElementById('clienteRegisterForm').addEventListener('submit', async function (e) {

            e.preventDefault();//manda al endpoint de registro y envia la informacion requerida

            const user = document.getElementById('registerUsername').value;
            const pass = document.getElementById('registerPassword').value;
            const errorDisplay = document.getElementById('clienteRegisterError');

            try {
                const res = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username: user, password: pass })//valida que cumpla con la info en formato payload
                });

                const data = await res.json();

                if (!res.ok) {//manejo de errores
                    errorDisplay.innerText = data.error || 'Error al registrar el usuario.';
                    errorDisplay.style.display = 'block';
                    return;
                }
              //guarda el jwt y almanacena la info del usuario
                localStorage.setItem('jwtToken', data.token);
                localStorage.setItem('jwtRole', data.role);
                localStorage.setItem('jwtUser', data.username);

                clienteModal.hide(); //esconde el modal y recarga la pagina
                location.href = "/";

            } catch (err) {
                console.error(err);//manejo de excepciones
                errorDisplay.innerText = 'Error de conexión al intentar registrar.';
                errorDisplay.style.display = 'block';
            }
        });
    }

    // visualiza el navbar segun el rol
    const token = localStorage.getItem('jwtToken');//obtine el token y rol del localstorage
    const role = localStorage.getItem('jwtRole');

    const loginItem = document.getElementById('nav-login-link');
    const userMenuItem = document.getElementById('nav-user-menu');

    const allNavItems = document.querySelectorAll(".nav-item:not(#nav-user-menu):not(#nav-login-link)");
//selecciona todos los elementos del navbar excepto el loginy menu

    const dashboardItem =
        document.querySelector("a.dropdown-item[href='/dashboard']")?.parentElement;//obtiene el elemento si existe

    

    if (!token) {//si no hay token o no esta logueado muestra todo y oculta el dashboard
        loginItem?.classList.remove("d-none");
        userMenuItem?.classList.add("d-none");

        allNavItems.forEach(i => i.style.display = "block");

        if (dashboardItem) dashboardItem.style.display = "none";
        
    }
    else if (role === "CLIENTE") {//si esta logueado muestra todo menos el dashboard
        loginItem?.classList.add("d-none");
        userMenuItem?.classList.remove("d-none");

        allNavItems.forEach(i => i.style.display = "block");

        if (dashboardItem) dashboardItem.style.display = "none";
      

        const userDropdown = userMenuItem.querySelector('#userDropdown');
        if (userDropdown) userDropdown.innerHTML = "👋 Hola, <b>Cliente</b>";
    }
    else if (role === "TRABAJADOR") {//si es trabajador muestra solo el dashboard
        loginItem?.classList.add("d-none");
        userMenuItem?.classList.remove("d-none");

        allNavItems.forEach(item => {
            const href = item.querySelector("a")?.getAttribute("href");
            if (href === "/dashboard") {
                item.style.display = "block";
            } else {
                item.style.display = "none";
            }
        });

        if (dashboardItem) dashboardItem.style.display = "block";
        

        const userDropdown = userMenuItem.querySelector('#userDropdown');
        if (userDropdown) userDropdown.innerHTML = "👋 Hola, <b>Trabajador</b>";
    }

    // cierre de sesion y limpieza del localstorage y retorna a la pagina principal
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function (e) {
            e.preventDefault();

            localStorage.removeItem('jwtToken');
            localStorage.removeItem('jwtRole');
            localStorage.removeItem('jwtUser');

            location.href = "/";
        });
    }

});

