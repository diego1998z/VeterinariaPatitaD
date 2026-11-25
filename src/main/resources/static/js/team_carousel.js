(() => {
    const track = document.querySelector('[data-team-carousel-track]');
    const previousButton = document.querySelector('[data-team-carousel-prev]');
    const nextButton = document.querySelector('[data-team-carousel-next]');
    const status = document.querySelector('[data-team-carousel-status]');

    if (!track || !previousButton || !nextButton || !status) return;

    const slides = Array.from(track.querySelectorAll('.team-carousel__slide'));
    if (!slides.length) return;

    const mobileQuery = window.matchMedia('(max-width: 767.98px)');
    let activeIndex = 0;
    let touchStartX = null;

    const updateStatus = () => {
        status.textContent = `Profesional ${activeIndex + 1} de ${slides.length}`;
    };

    const setSlideVisibility = () => {
        slides.forEach((slide, index) => {
            const isActive = index === activeIndex;
            slide.hidden = !isActive;
            slide.classList.toggle('is-active', isActive);
            slide.setAttribute('aria-hidden', String(!isActive));

            if ('inert' in slide) {
                slide.inert = !isActive;
            }
        });
    };

    const showAllSlides = () => {
        slides.forEach((slide) => {
            slide.hidden = false;
            slide.classList.remove('is-active');
            slide.removeAttribute('aria-hidden');

            if ('inert' in slide) {
                slide.inert = false;
            }
        });
    };

    const goTo = (index) => {
        if (!mobileQuery.matches) return;

        activeIndex = (index + slides.length) % slides.length;
        setSlideVisibility();
        updateStatus();
        slides[activeIndex].querySelector('h5, a, button, input')?.focus({ preventScroll: true });
    };

    const applyResponsiveState = () => {
        if (mobileQuery.matches) {
            setSlideVisibility();
        } else {
            showAllSlides();
        }

        updateStatus();
    };

    previousButton.addEventListener('click', () => goTo(activeIndex - 1));
    nextButton.addEventListener('click', () => goTo(activeIndex + 1));

    track.addEventListener('touchstart', (event) => {
        touchStartX = event.changedTouches[0]?.clientX ?? null;
    }, { passive: true });

    track.addEventListener('touchend', (event) => {
        if (touchStartX === null || !mobileQuery.matches) return;

        const touchEndX = event.changedTouches[0]?.clientX ?? touchStartX;
        const horizontalDistance = touchEndX - touchStartX;
        touchStartX = null;

        if (Math.abs(horizontalDistance) < 45) return;

        goTo(horizontalDistance < 0 ? activeIndex + 1 : activeIndex - 1);
    }, { passive: true });

    mobileQuery.addEventListener('change', applyResponsiveState);
    applyResponsiveState();
})();
