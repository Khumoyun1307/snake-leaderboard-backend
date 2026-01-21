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

// Smooth scroll anchors
document.querySelectorAll('a[href^="#"]').forEach(a => {
  a.addEventListener("click", (e) => {
    const id = a.getAttribute("href");
    if (!id || id === "#") return;
    const el = document.querySelector(id);
    if (!el) return;
    e.preventDefault();
    el.scrollIntoView({ behavior: "smooth", block: "start" });
  });
});

// Footer year
const y = document.getElementById("year");
if (y) y.textContent = String(new Date().getFullYear());

// Screenshot carousel (viewport is the scroll container)
const viewport = document.getElementById("shotsViewport");
const track = document.getElementById("shotsTrack");
const prevBtn = document.getElementById("shotsPrev");
const nextBtn = document.getElementById("shotsNext");

if (viewport && track && track.children.length > 0) {
  const slides = Array.from(track.children);
  let index = 0;

  function gapPx() {
    const style = getComputedStyle(track);
    const g = style.gap || style.columnGap || "0px";
    return parseFloat(g) || 0;
  }

  function stepPx() {
    const w = slides[0].getBoundingClientRect().width;
    return w + gapPx();
  }

  function go(i) {
    index = (i + slides.length) % slides.length;
    viewport.scrollTo({ left: index * stepPx(), behavior: "smooth" });
  }

  prevBtn?.addEventListener("click", () => go(index - 1));
  nextBtn?.addEventListener("click", () => go(index + 1));

  // Auto slide every 4s
  let timer = setInterval(() => go(index + 1), 4000);

  // Pause on hover
  viewport.addEventListener("mouseenter", () => clearInterval(timer));
  viewport.addEventListener("mouseleave", () => {
    clearInterval(timer);
    timer = setInterval(() => go(index + 1), 4000);
  });

  window.addEventListener("resize", () => go(index));
}
