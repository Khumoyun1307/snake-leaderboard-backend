// Mobile menu
const menuBtn = document.getElementById("menuBtn");
const mobileMenu = document.getElementById("mobileMenu");

if (menuBtn && mobileMenu) {
  menuBtn.setAttribute("aria-controls", "mobileMenu");
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

  mobileMenu.addEventListener("click", (event) => {
    const link = event.target.closest("a");
    if (!link) return;
    if (!mobileMenu.hasAttribute("hidden")) {
      mobileMenu.setAttribute("hidden", "");
      menuBtn.setAttribute("aria-expanded", "false");
    }
  });
}

// Reveal on scroll
const reveals = document.querySelectorAll(".reveal");
const io = new IntersectionObserver(
  (entries) => {
    for (const e of entries) {
      if (e.isIntersecting) {
        e.target.classList.add("show");
        io.unobserve(e.target);
      }
    }
  },
  { threshold: 0.12 }
);
reveals.forEach((el) => io.observe(el));

// Smooth scroll anchors
document.querySelectorAll('a[href^="#"]').forEach((a) => {
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

// Scroll to top button
const scrollTopBtn = document.getElementById("scrollTopBtn");
if (scrollTopBtn) {
  window.addEventListener("scroll", () => {
    if (window.scrollY > 200) {
      scrollTopBtn.removeAttribute("hidden");
    } else {
      scrollTopBtn.setAttribute("hidden", "");
    }
  });

  scrollTopBtn.addEventListener("click", () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  });
}
