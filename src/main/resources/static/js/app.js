(function () {
    function el(id) {
        return document.getElementById(id);
    }

    function filtersForm() {
        return el('filters');
    }

    function submitFilters() {
        var form = filtersForm();
        if (form && window.htmx) {
            window.htmx.trigger(form, 'filters-changed');
        }
    }

    function resetPage() {
        var page = el('page');
        if (page) {
            page.value = '0';
        }
    }

    function toast(message) {
        var box = el('toast');
        if (!box) {
            return;
        }
        box.textContent = message;
        box.hidden = false;
        clearTimeout(box.dataset.timer);
        box.dataset.timer = setTimeout(function () {
            box.hidden = true;
        }, 2500);
    }

    document.addEventListener('htmx:configRequest', function (event) {
        var token = document.querySelector('meta[name="_csrf"]');
        var header = document.querySelector('meta[name="_csrf_header"]');
        if (token && header && header.content) {
            event.detail.headers[header.content] = token.content;
        }
    });

    document.addEventListener('change', function (event) {
        var target = event.target;
        if (!target.closest || !target.closest('#filters')) {
            return;
        }
        if (target.id === 'page' || target.id === 'sort' || target.id === 'preset') {
            return;
        }
        resetPage();
    }, true);

    document.addEventListener('keyup', function (event) {
        if (event.target && event.target.id === 'q') {
            resetPage();
        }
    }, true);

    document.addEventListener('click', function (event) {
        var sortButton = event.target.closest('[data-sort]');
        if (sortButton) {
            event.preventDefault();
            var sortInput = el('sort');
            var wanted = sortButton.getAttribute('data-sort');
            var current = (sortInput.value || '').split(',');
            var ascending = current[0] === wanted && current[1] === 'asc';
            sortInput.value = wanted + (ascending ? ',desc' : ',asc');
            resetPage();
            submitFilters();
            return;
        }

        var pageButton = event.target.closest('[data-page-step]');
        if (pageButton) {
            event.preventDefault();
            var pageInput = el('page');
            var step = parseInt(pageButton.getAttribute('data-page-step'), 10);
            var next = parseInt(pageInput.value || '0', 10) + step;
            pageInput.value = String(next < 0 ? 0 : next);
            submitFilters();
            return;
        }

        var presetButton = event.target.closest('[data-preset]');
        if (presetButton) {
            event.preventDefault();
            var presetInput = el('preset');
            var wantedPreset = presetButton.getAttribute('data-preset');
            presetInput.value = presetInput.value === wantedPreset ? '' : wantedPreset;
            resetPage();
            submitFilters();
            return;
        }

        if (event.target.id === 'clear-filters') {
            event.preventDefault();
            var form = filtersForm();
            if (form) {
                form.reset();
                el('q').value = '';
                el('satellite').value = '';
                el('gender').value = '';
                el('civilStatus').value = '';
                el('inDgroup').value = '';
                el('includeArchived').checked = false;
                el('preset').value = '';
                resetPage();
                submitFilters();
            }
            return;
        }

        if (event.target.matches('[data-close-modal]')) {
            event.preventDefault();
            var modal = el('modal');
            if (modal) {
                modal.innerHTML = '';
            }
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            var modal = el('modal');
            if (modal && modal.innerHTML.trim() !== '') {
                modal.innerHTML = '';
            }
            return;
        }

        if (event.key === '/' && event.target === document.body) {
            var search = el('q');
            if (search) {
                event.preventDefault();
                search.focus();
                search.select();
            }
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Enter') {
            return;
        }
        var row = event.target.closest ? event.target.closest('tr.row-link') : null;
        if (row && window.htmx) {
            event.preventDefault();
            row.click();
        }
    });

    document.body.addEventListener('refresh-people', function () {
        toast('Saved');
    });
}());
