(() => {
  const els = {
    mapWrap: document.getElementById("mapFieldWrap"),
    mapLabel: document.getElementById("mapLabel"),
    mapId: document.getElementById("mapId"),
    mode: document.getElementById("mode"),
    difficulty: document.getElementById("difficulty"),
    limit: document.getElementById("limit"),
    search: document.getElementById("search"),
    thead: document.getElementById("lbThead"),
    tbody: document.getElementById("lbTbody"),
    banner: document.getElementById("lbBanner"),
    prevBtn: document.getElementById("prevBtn"),
    nextBtn: document.getElementById("nextBtn"),
    pageInfo: document.getElementById("lbPageInfo"),
    refreshBtn: document.getElementById("refreshBtn"),
  };

  if (!els.tbody || !els.thead) return;

  // Desktop defaults
  let pageSize = Number(els.limit?.value || 25);
  let offset = 0;
  let lastReturnedCount = 0;

  // Used to avoid firing refresh while we repopulate map options
  let updatingMap = false;

  // Persist selected map (like your GameSettings selectedMapId)
  const LS_KEY = "snake.selectedMapId";

  function showBanner(msg) {
    if (!els.banner) return;
    if (!msg) {
      els.banner.hidden = true;
      els.banner.textContent = "";
      return;
    }
    els.banner.hidden = false;
    els.banner.textContent = msg;
  }

  function esc(s) {
    return String(s ?? "").replace(/[&<>"']/g, (c) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;",
    }[c]));
  }

  function msToHuman(ms) {
    if (ms == null || ms <= 0) return "-";
    const sec = Math.floor(ms / 1000);
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return m > 0 ? `${m}m ${s}s` : `${sec}s`;
  }

  function whenFmt(iso) {
    if (!iso) return "-";
    try {
      // Similar to "MMM d, HH:mm" feel
      const d = new Date(iso);
      return d.toLocaleString(undefined, {
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return "-";
    }
  }

  function getMode() {
    return (els.mode?.value || "STANDARD").trim();
  }
  function getDiff() {
    return (els.difficulty?.value || "ANY").trim();
  }
  function getMapId() {
    const v = Number(els.mapId?.value ?? 0);
    return Number.isFinite(v) ? v : 0;
  }

  // ---- Mode -> Map behavior (matches desktop) ----
  function setMapOptionsForMode() {
    const mode = getMode();

    updatingMap = true;

    // Clear options
    els.mapId.innerHTML = "";

    if (mode === "STANDARD") {
      // STANDARD uses Basic (mapId=0). Map disabled but visible.
      els.mapWrap.style.display = "";
      els.mapLabel.textContent = "Map";
      addMapOption(0, "Basic");
      els.mapId.value = "0";
      els.mapId.disabled = true;
      els.mapId.title = "STANDARD uses the basic map.";

    } else if (mode === "MAP_SELECT") {
      // MAP_SELECT supports ANY + 1..10, default to saved map
      els.mapWrap.style.display = "";
      els.mapLabel.textContent = "Map";
      addMapOption(0, "ANY");
      for (let i = 1; i <= 10; i++) addMapOption(i, String(i));

      let saved = Number(localStorage.getItem(LS_KEY) || "1");
      if (!Number.isFinite(saved) || saved < 1 || saved > 10) saved = 1;
      els.mapId.value = String(saved);

      els.mapId.disabled = false;
      els.mapId.title = "Choose a map, or ANY to view all maps.";

    } else if (mode === "RACE") {
      // RACE hides map completely and forces mapId=0
      els.mapWrap.style.display = "none";
      addMapOption(0, "0");
      els.mapId.value = "0";
      els.mapId.disabled = true;
      els.mapId.title = "";
    }

    updatingMap = false;
  }

  function addMapOption(value, label) {
    const opt = document.createElement("option");
    opt.value = String(value);
    opt.textContent = label;
    els.mapId.appendChild(opt);
  }

  // ---- Dynamic columns (matches desktop updateTableColumnsForCurrentFilters) ----
  function computeColumns() {
    const mode = getMode();
    const diff = getDiff();
    const mapId = getMapId();

    const race = mode === "RACE";
    const anyDiff = diff.toUpperCase() === "ANY";
    const mapSelectAnyMap = (mode === "MAP_SELECT" && mapId === 0);

    if (race && anyDiff) return ["Rank", "Name", "Map", "Diff", "Score", "Time", "When"];
    if (race) return ["Rank", "Name", "Map", "Score", "Time", "When"];
    if (mapSelectAnyMap && anyDiff) return ["Rank", "Name", "Map", "Diff", "Score", "Time", "When"];
    if (mapSelectAnyMap) return ["Rank", "Name", "Map", "Score", "Time", "When"];
    if (anyDiff) return ["Rank", "Name", "Diff", "Score", "Time", "When"];
    return ["Rank", "Name", "Score", "Time", "When"];
  }

  function renderThead(cols) {
    els.thead.innerHTML = `
      <tr>
        ${cols.map(c => `<th class="${c === "Rank" || c === "Score" || c === "Time" ? "num" : ""}">${esc(c)}</th>`).join("")}
      </tr>
    `;
  }

  function renderLoading(cols) {
    renderThead(cols);
    els.tbody.innerHTML = `<tr><td colspan="${cols.length}" class="muted">Loading leaderboard…</td></tr>`;
  }

  function renderEmpty(cols) {
    renderThead(cols);
    // Like desktop: show "No scores yet" in Name column
    const row = cols.map((c, i) => {
      if (i === 0) return `<td class="num">-</td>`;
      if (i === 1) return `<td>No scores yet</td>`;
      return `<td>-</td>`;
    }).join("");
    els.tbody.innerHTML = `<tr>${row}</tr>`;
  }

  function renderRows(cols, entries, query) {
    // Search filter (extra feature)
    const q = (query || "").trim().toLowerCase();
    const filtered = !q
      ? entries
      : entries.filter(e => String(e.playerName || "").toLowerCase().includes(q));

    if (!filtered.length) {
      renderEmpty(cols);
      lastReturnedCount = 0;
      updatePager();
      return;
    }

    els.tbody.innerHTML = filtered.map(e => {
      const time = msToHuman(e.timeSurvivedMs);
      const when = whenFmt(e.createdAt);
      const entryDiff = (e.difficulty == null || String(e.difficulty).trim() === "") ? "-" : e.difficulty;

      const map = e.mapId ?? "-";

      // Build row based on visible columns
      const cells = cols.map(c => {
        switch (c) {
          case "Rank": return `<td class="num">${e.rank ?? "-"}</td>`;
          case "Name": return `<td>${esc(e.playerName)}</td>`;
          case "Map": return `<td class="num">${map}</td>`;
          case "Diff": return `<td>${esc(entryDiff)}</td>`;
          case "Score": return `<td class="num">${e.score ?? "-"}</td>`;
          case "Time": return `<td class="num">${esc(time)}</td>`;
          case "When": return `<td>${esc(when)}</td>`;
          default: return `<td>-</td>`;
        }
      }).join("");

      return `<tr>${cells}</tr>`;
    }).join("");
  }

  function updatePager() {
    if (els.prevBtn) els.prevBtn.disabled = offset <= 0;
    if (els.nextBtn) els.nextBtn.disabled = lastReturnedCount < pageSize;
  }

  function updateStatusText() {
    // Desktop shows "Showing X–Y"
    const start = offset + 1;
    const end = offset + Math.max(lastReturnedCount, 0);
    els.pageInfo.textContent = lastReturnedCount
      ? `Showing ${start}–${end} (page size ${pageSize})`
      : `Showing — (page size ${pageSize})`;
  }

  async function refresh() {
    showBanner("");

    const mode = getMode();
    let mapId = getMapId();
    const diff = getDiff();

    // Desktop logic: if RACE => mapId forced to 0
    if (mode === "RACE") mapId = 0;

    pageSize = Number(els.limit?.value || 25);

    const cols = computeColumns();
    renderLoading(cols);

    // Build params exactly like backend expects
    const params = new URLSearchParams({
      mapId: String(mapId),
      mode: mode,
      limit: String(pageSize),
      offset: String(offset),
    });

    // CRITICAL: diff ANY must NOT be sent, otherwise backend may treat it as literal "ANY"
    if (diff && diff.toUpperCase() !== "ANY") {
      params.set("difficulty", diff);
    }

    try {
      const res = await fetch(`/api/leaderboard?${params.toString()}`, {
        headers: { "Accept": "application/json" }
      });

      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status}${txt ? ` — ${txt}` : ""}`);
      }

      const data = await res.json();
      const entries = Array.isArray(data.entries) ? data.entries : [];

      lastReturnedCount = entries.length;
      renderThead(cols);
      renderRows(cols, entries, els.search?.value || "");
      updatePager();
      updateStatusText();
    } catch (e) {
      lastReturnedCount = 0;
      updatePager();
      updateStatusText();
      showBanner(`Failed to load leaderboard (offline?). ${e?.message || e}`);
      renderEmpty(cols);
    }
  }

  function debounce(fn, ms) {
    let t;
    return (...args) => {
      clearTimeout(t);
      t = setTimeout(() => fn(...args), ms);
    };
  }

  // ---- Events (match desktop behavior) ----
  els.refreshBtn?.addEventListener("click", () => {
    offset = 0;
    refresh();
  });

  els.prevBtn?.addEventListener("click", () => {
    offset = Math.max(0, offset - pageSize);
    refresh();
  });

  els.nextBtn?.addEventListener("click", () => {
    if (lastReturnedCount < pageSize) return;
    offset = offset + pageSize;
    refresh();
  });

  els.mode?.addEventListener("change", () => {
    offset = 0;
    setMapOptionsForMode();
    refresh();
  });

  els.mapId?.addEventListener("change", () => {
    if (updatingMap) return;
    offset = 0;

    // Save map only in MAP_SELECT and only if mapId > 0 (desktop behavior)
    const mode = getMode();
    const mapId = getMapId();
    if (mode === "MAP_SELECT" && mapId > 0) {
      localStorage.setItem(LS_KEY, String(mapId));
    }

    refresh();
  });

  els.difficulty?.addEventListener("change", () => {
    offset = 0;
    refresh();
  });

  els.limit?.addEventListener("change", () => {
    offset = 0;
    refresh();
  });

  // Search is client-side filter on returned page
  els.search?.addEventListener("input", debounce(() => refresh(), 160));

  // Init
  // Set defaults like desktop
  els.mode.value = "STANDARD";
  els.difficulty.value = "ANY";
  if (els.limit) els.limit.value = "25";

  setMapOptionsForMode();
  refresh();
})();
