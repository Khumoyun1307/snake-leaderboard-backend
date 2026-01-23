(() => {
  const viewport = document.getElementById("coverflowViewport");
  const track = document.getElementById("coverflowTrack");
  const prevBtn = document.getElementById("coverflowPrev");
  const nextBtn = document.getElementById("coverflowNext");
  const wrapper = viewport ? viewport.closest(".coverflow") : null;

  if (!viewport || !track || !wrapper) return;

  const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)");
  const autoplayEnabled = !prefersReducedMotion.matches;
  const autoInterval = 4200;
  const manualResumeDelay = 5200;

  const cardData = [
    {
      title: "Neo Arcade",
      subtitle: "A neon glow with deliberate shadows and a retro pulse.",
      tag: "Arcade",
      bg: "radial-gradient(140% 90% at 20% 10%, rgba(120,166,255,0.45), transparent 55%), linear-gradient(140deg, rgba(12,18,28,0.95), rgba(24,36,52,0.95))"
    },
    {
      title: "Circuit Drift",
      subtitle: "Clean lines, icy blues, and a quiet sense of speed.",
      tag: "Synth",
      bg: "radial-gradient(140% 90% at 85% 20%, rgba(64,242,201,0.35), transparent 55%), linear-gradient(160deg, rgba(10,16,24,0.95), rgba(22,30,44,0.95))"
    },
    {
      title: "Vector Field",
      subtitle: "A grid of motion and a soft glow in the distance.",
      tag: "Grid",
      bg: "radial-gradient(140% 90% at 30% 80%, rgba(255,211,110,0.32), transparent 55%), linear-gradient(150deg, rgba(12,16,26,0.95), rgba(26,28,40,0.95))"
    },
    {
      title: "Night Run",
      subtitle: "Warm highlights, deep blacks, and a late-night loop.",
      tag: "Drive",
      bg: "radial-gradient(140% 90% at 70% 20%, rgba(120,90,255,0.28), transparent 55%), linear-gradient(160deg, rgba(8,10,16,0.95), rgba(18,20,30,0.95))"
    },
    {
      title: "Signal Bloom",
      subtitle: "Bright accents, subtle grain, and a sharp horizon.",
      tag: "Signal",
      bg: "radial-gradient(140% 90% at 30% 20%, rgba(255,140,120,0.30), transparent 55%), linear-gradient(155deg, rgba(10,12,18,0.95), rgba(26,24,30,0.95))"
    },
    {
      title: "Skyline Pulse",
      subtitle: "A distant skyline with quiet, electric accents.",
      tag: "City",
      bg: "radial-gradient(140% 90% at 70% 75%, rgba(120,166,255,0.28), transparent 55%), linear-gradient(150deg, rgba(10,14,22,0.95), rgba(20,26,38,0.95))"
    }
  ];

  const cards = cardData.map((item, index) => {
    const card = document.createElement("article");
    card.className = "coverflow-card";
    card.dataset.index = String(index);
    card.setAttribute("role", "group");
    card.setAttribute("aria-roledescription", "slide");
    card.setAttribute("aria-label", `${item.title} - ${item.subtitle}`);

    const art = document.createElement("div");
    art.className = "coverflow-art";
    art.style.background = item.bg;
    art.setAttribute("aria-hidden", "true");

    const content = document.createElement("div");
    content.className = "coverflow-content";

    const tag = document.createElement("span");
    tag.className = "coverflow-tag";
    tag.textContent = item.tag;

    const title = document.createElement("h3");
    title.className = "coverflow-title";
    title.textContent = item.title;

    const subtitle = document.createElement("p");
    subtitle.className = "coverflow-subtitle";
    subtitle.textContent = item.subtitle;

    content.append(tag, title, subtitle);
    card.append(art, content);
    track.append(card);
    return card;
  });

  const state = {
    currentIndex: 0,
    targetIndex: 0,
    queuedDelta: 0,
    isAnimating: false,
    isHovering: false,
    autoTimer: null,
    manualCooldownUntil: 0
  };

  function wrapIndex(index) {
    const total = cards.length;
    return (index + total) % total;
  }

  function getOffset(index, activeIndex) {
    const total = cards.length;
    let offset = index - activeIndex;
    const half = Math.floor(total / 2);
    if (offset > half) offset -= total;
    if (offset < -half) offset += total;
    return offset;
  }

  function getMaxVisibleOffset() {
    return 1;
  }

  function getCardGap() {
    const width = viewport.getBoundingClientRect().width;
    const cardWidth = cards[0]?.offsetWidth || 300;
    const extra = width < 600 ? 32 : width < 900 ? 40 : 48;
    return cardWidth + extra;
  }

  function renderPositions() {
    const gap = getCardGap();
    const maxOffset = getMaxVisibleOffset();
    const activeIndex = state.currentIndex;

    cards.forEach((card, index) => {
      const offset = getOffset(index, activeIndex);
      const absOffset = Math.abs(offset);
      const clamped = Math.min(absOffset, 3);
      const scale = 1 - clamped * 0.12;
      const translateY = absOffset === 0 ? -10 : absOffset === 1 ? -2 : 8;
      const rotateY = offset === 0 ? 0 : offset > 0 ? -6 : 6;
      const opacity = absOffset === 0 ? 1 : absOffset === 1 ? 0.78 : absOffset === 2 ? 0.46 : 0;
      const isVisible = absOffset <= maxOffset;

      card.style.transform = `translate(-50%, -50%) translateX(${offset * gap}px) translateY(${translateY}px) rotateY(${rotateY}deg) scale(${scale})`;
      card.style.opacity = isVisible ? String(opacity) : "0";
      card.style.zIndex = String(10 - absOffset);
      card.style.filter = absOffset === 0 ? "saturate(1.02) brightness(1)" : "saturate(0.9) brightness(0.92)";
      card.style.pointerEvents = absOffset <= 1 ? "auto" : "none";

      card.classList.toggle("is-active", absOffset === 0);
      card.classList.toggle("is-near", absOffset === 1);
      card.classList.toggle("is-far", false);

      card.setAttribute("aria-hidden", isVisible ? "false" : "true");
      if (absOffset === 0) {
        card.setAttribute("aria-current", "true");
      } else {
        card.removeAttribute("aria-current");
      }
    });
  }

  function clearAutoplay() {
    if (state.autoTimer) {
      clearTimeout(state.autoTimer);
      state.autoTimer = null;
    }
  }

  function scheduleAutoplay(delay = autoInterval) {
    if (!autoplayEnabled) return;
    clearAutoplay();
    if (state.isHovering || state.isAnimating) return;
    const wait = Math.max(delay, state.manualCooldownUntil - Date.now());
    state.autoTimer = setTimeout(() => requestMove(1, "auto"), wait);
  }

  function noteManualInteraction() {
    state.manualCooldownUntil = Date.now() + manualResumeDelay;
    clearAutoplay();
  }

  function startTransition(nextIndex) {
    state.isAnimating = true;
    state.currentIndex = nextIndex;
    renderPositions();
  }

  // Lock + queue: one transition runs at a time; extra requests are merged into a single queued delta.
  // The carousel always moves exactly one slide per transition, then checks the queue for the next step.
  function requestMove(delta, source = "user") {
    if (!delta) return;
    if (source === "user") {
      noteManualInteraction();
    }

    if (source === "auto" && (state.isAnimating || state.queuedDelta !== 0)) {
      scheduleAutoplay(autoInterval);
      return;
    }

    if (state.isAnimating) {
      state.queuedDelta += delta;
      state.targetIndex = wrapIndex(state.targetIndex + delta);
      return;
    }

    if (!wrapper.classList.contains("is-ready")) {
      wrapper.classList.add("is-ready");
    }
    state.queuedDelta = 0;
    state.targetIndex = wrapIndex(state.currentIndex + delta);
    startTransition(state.targetIndex);
  }

  function handleQueuedMove() {
    if (state.queuedDelta === 0) {
      scheduleAutoplay(autoInterval);
      return;
    }
    const step = state.queuedDelta > 0 ? 1 : -1;
    state.queuedDelta -= step;
    state.targetIndex = wrapIndex(state.currentIndex + step);
    startTransition(state.targetIndex);
  }

  track.addEventListener("transitionend", (event) => {
    if (event.propertyName !== "transform") return;
    if (!state.isAnimating) return;
    if (event.target !== cards[state.currentIndex]) return;
    state.isAnimating = false;
    handleQueuedMove();
  });

  prevBtn?.addEventListener("click", () => requestMove(-1, "user"));
  nextBtn?.addEventListener("click", () => requestMove(1, "user"));

  viewport.addEventListener("keydown", (event) => {
    if (event.key === "ArrowLeft") {
      event.preventDefault();
      requestMove(-1, "user");
    }
    if (event.key === "ArrowRight") {
      event.preventDefault();
      requestMove(1, "user");
    }
  });

  viewport.addEventListener("mouseenter", () => {
    state.isHovering = true;
    clearAutoplay();
  });

  viewport.addEventListener("mouseleave", () => {
    state.isHovering = false;
    scheduleAutoplay(autoInterval);
  });

  window.addEventListener("resize", () => {
    renderPositions();
  });

  state.targetIndex = state.currentIndex;
  renderPositions();
  requestAnimationFrame(() => wrapper.classList.add("is-ready"));
  scheduleAutoplay(autoInterval);
})();
