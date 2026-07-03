const slides = document.querySelectorAll(".banner-slide");
const nextBtns = document.querySelectorAll(".next-btn");
const prevBtns = document.querySelectorAll(".prev-btn");

let currentIndex = 0;

function showSlide(index){
    slides.forEach(slide => {
        slide.classList.remove("active");
    });

    slides[index].classList.add("active");
}

nextBtns.forEach(btn => {
    btn.addEventListener("click", () => {
        currentIndex++;

        if(currentIndex >= slides.length){
            currentIndex = 0;
        }

        showSlide(currentIndex);
    });
});

prevBtns.forEach(btn => {
    btn.addEventListener("click", () => {
        currentIndex--;

        if(currentIndex < 0){
            currentIndex = slides.length - 1;
        }

        showSlide(currentIndex);
    });
});

/* 자동 슬라이드 (5초) */
setInterval(() => {
    currentIndex++;

    if(currentIndex >= slides.length){
        currentIndex = 0;
    }

    showSlide(currentIndex);
}, 5000);