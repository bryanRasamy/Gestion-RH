(function () {
    'use strict';

    /* ============================================================
       État
       ============================================================ */
    let DEPARTEMENTS = [];
    const postesParDept = {};   // cache : idDepartement -> liste de postes
    let nextId = 1;

    // Liste des diplômes disponibles dans la liste déroulante
    let DIPLOMES = [];

    /* ============================================================
       DOM
       ============================================================ */
    const form          = document.getElementById('requestForm');
    const rowsEl        = document.getElementById('rows');
    const addBtn        = document.getElementById('addRow');
    const countEl       = document.getElementById('count');
    const submitBtn     = document.getElementById('submitBtn');
    const success       = document.getElementById('formSuccess');
    const deadline      = document.getElementById('deadline');

    /* ============================================================
       Date limite
       ============================================================ */
    const now = new Date();
    const todayISO = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString().split('T')[0];
    deadline.min = todayISO;

    deadline.addEventListener('input', () => {
        deadline.classList.remove('is-invalid');
        const deadlineBlock = document.getElementById('deadlineBlock');
        const deadlineError = document.getElementById('deadlineError');
        if (deadlineBlock) deadlineBlock.classList.remove('has-error');
        if (deadlineError) deadlineError.textContent = '';
        if (success) success.classList.remove('is-visible');
    });

    /* ============================================================
       Chargement des données (API REST)
       ============================================================ */
    async function fetchJson(url) {
        const r = await fetch(url, { headers: { 'Accept': 'application/json' } });
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    }

    async function chargerDepartements() {
        try {
            DEPARTEMENTS = await fetchJson('/api/departements');
        } catch (e) {
            console.error('Impossible de charger les départements', e);
            DEPARTEMENTS = [];
        }
    }

    async function chargerDiplomes() {
        try {
            DIPLOMES = await fetchJson('/api/diplomes');
        } catch (e) {
            console.error('Impossible de charger les diplomes', e);
            DIPLOMES = [];
        }
    }

    async function chargerPostes(idDepartement) {
        if (postesParDept[idDepartement]) return postesParDept[idDepartement];
        try {
            const postes = await fetchJson(
                '/api/postes?idDepartement=' + encodeURIComponent(idDepartement));
            postesParDept[idDepartement] = postes;
            return postes;
        } catch (e) {
            console.error('Impossible de charger les postes', e);
            return [];
        }
    }

    /* ============================================================
       Utilitaires
       ============================================================ */
    function esc(s) {
        return String(s).replace(/[&<>"']/g, c => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[c]));
    }

    function optionsHtml(liste) {
        return liste.map(o => `<option value="${o.id}">${esc(o.libelle)}</option>`).join('');
    }

    function optionsDiplomesHtml() {
        return DIPLOMES.map(d => `<option value="${esc(d.id)}">${esc(d.nomDiplome)}</option>`).join('');
    }

    async function remplirPostes(row) {
        const dept = document.getElementById("departementId");
        const sel  = row.querySelector('.poste');
        if (!dept || !sel) return;
        
        const idDept = dept.value;
        sel.innerHTML = '<option value="">Sélectionner un poste…</option>';
        if (!idDept) return;

        const postes = await chargerPostes(idDept);
        if (dept.value !== idDept) return;
        sel.insertAdjacentHTML('beforeend', optionsHtml(postes));
    }

    /* ============================================================
       Construction d'une ligne
       ============================================================ */
    function createRow() {
        const n = nextId++;
        const row = document.createElement('div');
        row.className = 'req-row';
        row.dataset.id = n;
        row.innerHTML = `
      <span class="req-row__index" aria-hidden="true"></span>

      <div class="req-row__field req-row__field--dept">
        <label for="dept-${n}" class="req-row__label-mobile">Département</label>
        <select id="dept-${n}" name="detailDemandes[0].departement.id"
                class="departement" required>
          ${optionsHtml(DEPARTEMENTS)}
        </select>
      </div>

      <div class="req-row__field req-row__field--poste">
        <label for="poste-${n}" class="req-row__label-mobile">Poste</label>
        <select id="poste-${n}" name="detailDemandes[0].poste.id"
                class="poste" required>
          <option value="">Sélectionner un poste…</option>
        </select>
      </div>

      <div class="req-row__field req-row__field--diplome">
        <label for="diplome-${n}" class="req-row__label-mobile">Diplôme</label>
        <select id="diplome-${n}" name="detailDemandes[0].diplome"
                class="diplome" required>
          <option value="">Sélectionner un diplôme…</option>
          ${optionsDiplomesHtml()}
        </select>
      </div>

      <div class="req-row__field req-row__field--exp">
        <label for="exp-${n}" class="req-row__label-mobile">Exp. (ans)</label>
        <input type="number" id="exp-${n}" name="detailDemandes[0].anneeExperience"
               class="exp" min="0" step="1" value="0" required inputmode="numeric">
      </div>

      <div class="req-row__field req-row__field--num">
        <label for="nombre-${n}" class="req-row__label-mobile">Nombre</label>
        <input type="number" id="nombre-${n}" name="detailDemandes[0].nbrPersonnel"
               class="nombre" min="1" step="1" value="1" required inputmode="numeric">
      </div>

      <div class="req-row__field req-row__field--motif">
        <label for="motif-${n}" class="req-row__label-mobile">Motif</label>
        <input type="text" id="motif-${n}" name="detailDemandes[0].motif"
               class="motif" placeholder="Ex : renfort projet" autocomplete="off" required>
      </div>

      <button type="button" class="req-row__remove" aria-label="Supprimer ce poste">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2.2"
             stroke-linecap="round" aria-hidden="true">
          <path d="M5 12h14"/>
        </svg>
      </button>
    `;
        return row;
    }

    /* ============================================================
       Mise à jour de l'état + réindexation des name
       ============================================================ */
    function updateState() {
        const rows = Array.from(rowsEl.querySelectorAll('.req-row'));

        rows.forEach((row, i) => {
            const indexEl = row.querySelector('.req-row__index');
            if (indexEl) indexEl.textContent = String(i + 1).padStart(2, '0');

            row.querySelectorAll('[name]').forEach(champ => {
                champ.name = champ.name.replace(
                    /^detailDemandes\[\d+\]/, 'detailDemandes[' + i + ']');
            });

            const removeBtn = row.querySelector('.req-row__remove');
            if (removeBtn) removeBtn.disabled = rows.length === 1;
        });

        let totalPersonnes = 0;
        rows.forEach(row => {
            const nombreInput = row.querySelector('.nombre');
            if (!nombreInput) return;
            const v = parseInt(nombreInput.value, 10);
            if (!isNaN(v) && v > 0) totalPersonnes += v;
        });

        const nPostes = rows.length;
        if (countEl) {
            countEl.textContent =
                String(nPostes).padStart(2, '0') +
                (nPostes > 1 ? ' postes' : ' poste') +
                ' · ' +
                String(totalPersonnes).padStart(2, '0') +
                (totalPersonnes > 1 ? ' personnes' : ' personne');
        }
    }

    /* ============================================================
       Ajout / suppression
       ============================================================ */
    async function addRow(focus = true) {
        const lastRow = rowsEl.querySelector('.req-row:last-child');
        const row = createRow();

        if (lastRow) {
            const lastDept = lastRow.querySelector('.departement').value;
            if (lastDept) row.querySelector('.departement').value = lastDept;
        }

        rowsEl.appendChild(row);
        updateState();
        await remplirPostes(row);

        if (focus) {
            const select = row.querySelector('.poste');
            if (select) {
                select.focus();
                select.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        }
    }

    function removeRow(row) {
        const rows = rowsEl.querySelectorAll('.req-row');
        if (rows.length <= 1) return;

        const prev = row.previousElementSibling;
        const next = row.nextElementSibling;

        row.classList.add('is-removing');
        setTimeout(() => {
            row.remove();
            updateState();

            const target = (prev && prev.classList.contains('req-row'))
                ? prev
                : (next && next.classList.contains('req-row') ? next : null);
            if (target) {
                const removeBtn = target.querySelector('.req-row__remove');
                if (removeBtn) removeBtn.focus();
            }
        }, 280);
    }

    /* ============================================================
       Écouteurs
       ============================================================ */
    if (addBtn) {
        addBtn.addEventListener('click', () => addRow());
    }

    rowsEl.addEventListener('click', (e) => {
        const btn = e.target.closest('.req-row__remove');
        if (btn && !btn.disabled) {
            removeRow(btn.closest('.req-row'));
        }
    });

    rowsEl.addEventListener('input', (e) => {
        const field = e.target;
        if (field.matches('input, select')) {
            field.classList.remove('is-invalid');
        }
        if (field.matches('.nombre, .exp')) {
            if (field.value && parseInt(field.value, 10) < 0) field.value = 0;
            if (field.matches('.nombre')) updateState();
        }
    });

    rowsEl.addEventListener('change', (e) => {
        if (e.target.matches('select, input')) {
            e.target.classList.remove('is-invalid');
        }
        if (e.target.matches('.departement')) {
            remplirPostes(e.target.closest('.req-row'));
        }
    });

    /* ============================================================
       Soumission : validation puis envoi réel au serveur
       ============================================================ */
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        if (success) success.classList.remove('is-visible');

        /* --- Validation date limite --- */
        let ok = true;
        const deadlineVal = deadline.value;
        const deadlineBlock = document.getElementById('deadlineBlock');
        const deadlineError = document.getElementById('deadlineError');

        deadline.classList.remove('is-invalid');
        if (deadlineBlock) deadlineBlock.classList.remove('has-error');
        if (deadlineError) deadlineError.textContent = '';

        if (!deadlineVal) {
            deadline.classList.add('is-invalid');
            if (deadlineBlock) deadlineBlock.classList.add('has-error');
            if (deadlineError) deadlineError.textContent = 'Champ requis';
            ok = false;
        } else if (deadlineVal < todayISO) {
            deadline.classList.add('is-invalid');
            if (deadlineBlock) deadlineBlock.classList.add('has-error');
            if (deadlineError) deadlineError.textContent = 'Date passée';
            ok = false;
        }

        /* --- Validation des lignes --- */
        rowsEl.querySelectorAll('.req-row').forEach(row => {
            const dept    = row.querySelector('.departement');
            const sel     = row.querySelector('.poste');
            const diplome = row.querySelector('.diplome');
            const exp     = row.querySelector('.exp');
            const num     = row.querySelector('.nombre');
            const motif   = row.querySelector('.motif');

            [dept, sel, diplome, exp, num, motif].forEach(f => {
                if (f) f.classList.remove('is-invalid');
            });

            const nExp = exp ? parseInt(exp.value, 10) : 0;
            const nNum = num ? parseInt(num.value, 10) : 0;

            if (!dept || !dept.value)               { if (dept) dept.classList.add('is-invalid');    ok = false; }
            if (!sel || !sel.value)                 { if (sel) sel.classList.add('is-invalid');     ok = false; }
            if (!diplome || !diplome.value)         { if (diplome) diplome.classList.add('is-invalid'); ok = false; }
            if (isNaN(nExp) || nExp < 0)            { if (exp) exp.classList.add('is-invalid');     ok = false; }
            if (isNaN(nNum) || nNum < 1)            { if (num) num.classList.add('is-invalid');     ok = false; }
            if (!motif || !motif.value.trim())      { if (motif) motif.classList.add('is-invalid');   ok = false; }
        });

        /* --- Focus sur la première erreur --- */
        if (!ok) {
            if (deadline.classList.contains('is-invalid')) {
                deadline.focus();
                if (deadlineBlock) deadlineBlock.scrollIntoView({ behavior: 'smooth', block: 'center' });
            } else {
                const firstInvalid = rowsEl.querySelector('.is-invalid');
                if (firstInvalid) firstInvalid.focus();
            }
            return;
        }

        /* --- Envoi réel --- */
        updateState();                       // indices 0..n-1 sans trou
        if (submitBtn) {
            submitBtn.classList.add('is-loading');
            submitBtn.disabled = true;
        }
        form.submit();                       // ne redéclenche pas l'évènement "submit"
    });

    /* ============================================================
       Navbar mobile
       ============================================================ */
    const nav    = document.getElementById('appNav');
    const toggle = document.getElementById('appNavToggle');
    if (nav && toggle) {
        toggle.addEventListener('click', () => {
            const open = nav.classList.toggle('is-open');
            toggle.setAttribute('aria-expanded', open);
            toggle.setAttribute('aria-label', open ? 'Fermer le menu' : 'Ouvrir le menu');
        });
        document.querySelectorAll('.app-nav__link').forEach(link => {
            link.addEventListener('click', () => {
                nav.classList.remove('is-open');
                toggle.setAttribute('aria-expanded', 'false');
            });
        });
    }

    /* ============================================================
       Démarrage : départements d'abord, puis la première ligne
       ============================================================ */
    Promise.all([chargerDepartements(), chargerDiplomes()])
    .then(() => addRow(false));

})();