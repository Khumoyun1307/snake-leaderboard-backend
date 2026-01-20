// Mobile menu
const menuBtn = document.getElementById("menuBtn");
const mobileMenu = document.getElementById("mobileMenu");

if (menuBtn && mobileMenu) {
  menuBtn.addEventListener("click", () => {
    const open = !mobileMenu.hasAttribute("hidden");
    if (open) {
      mobileMenu.setAttribute("hidden", "");
      menuBtn.setAttribute("aria-expanded", "false");
    } else {
      mobileMenu.removeAttribute("hidden");
      menuBtn.setAttribute("aria-expanded", "true");
    }
  });
}

// Reveal on scroll
const reveals = document.querySelectorAll(".reveal");
const io = new IntersectionObserver((entries) => {
  for (const e of entries) {
    if (e.isIntersecting) {
      e.target.classList.add("show");
      io.unobserve(e.target);
    }
  }
}, { threshold: 0.12 });

reveals.forEach(el => io.observe(el));

// Carousel
const shots = document.getElementById("shots");
const prevShot = document.getElementById("prevShot");
const nextShot = document.getElementById("nextShot");
const dotsWrap = document.getElementById("shotDots");

if (shots && dotsWrap) {
  const slides = Array.from(shots.children);
  let index = 0;

  // create dots
  const dots = slides.map((_, i) => {
    const d = document.createElement("div");
    d.className = "dot" + (i === 0 ? " active" : "");
    d.addEventListener("click", () => go(i));
    dotsWrap.appendChild(d);
    return d;
  });

  function go(i) {
    index = Math.max(0, Math.min(slides.length - 1, i));
    shots.scrollTo({ left: index * shots.clientWidth, behavior: "smooth" });
    dots.forEach((d, di) => d.classList.toggle("active", di === index));
  }

  function next() { go(index + 1); }
  function prev() { go(index - 1); }

  nextShot?.addEventListener("click", next);
  prevShot?.addEventListener("click", prev);

  // keep index in sync on resize
  window.addEventListener("resize", () => go(index));

  // optional: auto-advance (comment out if you don't want it)
  let t = setInterval(() => go((index + 1) % slides.length), 5500);
  shots.addEventListener("mouseenter", () => clearInterval(t));
  shots.addEventListener("mouseleave", () => t = setInterval(() => go((index + 1) % slides.length), 5500));
}
