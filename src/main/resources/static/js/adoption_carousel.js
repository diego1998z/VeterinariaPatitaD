document.addEventListener('DOMContentLoaded', () => {
    const carousel = document.getElementById('adoption-pets-carousel');
    const controls = document.querySelectorAll('[data-carousel-direction]');
    const status = document.querySelector('.adoption-carousel__status');

    if (!carousel || !status || controls.length !== 2) {
        return;
    }

    const cards = Array.from(carousel.querySelectorAll('.INFO-PERRITOS'));
    const mobileQuery = window.matchMedia('(max-width: 767.98px)');

    const updateStatus = () => {
        if (!mobileQuery.matches || cards.length === 0) {
            return;
        }

        const closestIndex = cards.reduce((currentIndex, card, index) => {
            const currentDistance = Math.abs(cards[currentIndex].offsetLeft - carousel.scrollLeft);
            const candidateDistance = Math.abs(card.offsetLeft - carousel.scrollLeft);
            return candidateDistance < currentDistance ? index : currentIndex;
        }, 0);

        status.textContent = `Mascota ${closestIndex + 1} de ${cards.length}`;
    };

    controls.forEach((control) => {
        control.addEventListener('click', () => {
            if (!mobileQuery.matches || cards.length === 0) {
                return;
            }

            const currentIndex = cards.reduce((closest, card, index) => {
                return Math.abs(card.offsetLeft - carousel.scrollLeft) < Math.abs(cards[closest].offsetLeft - carousel.scrollLeft)
                    ? index
                    : closest;
            }, 0);
            const direction = control.dataset.carouselDirection === 'next' ? 1 : -1;
            const targetIndex = (currentIndex + direction + cards.length) % cards.length;

            cards[targetIndex].scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
        });
    });

    carousel.addEventListener('scroll', () => window.requestAnimationFrame(updateStatus), { passive: true });
    mobileQuery.addEventListener('change', updateStatus);
    updateStatus();
});
