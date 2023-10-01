/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

var moduleSearchIndex;
var packageSearchIndex;
var typeSearchIndex;
var memberSearchIndex;
var tagSearchIndex;
function loadScripts(doc, tag) {
    createElem(doc, tag, 'search.js');

    createElem(doc, tag, 'module-search-index.js');
    createElem(doc, tag, 'package-search-index.js');
    createElem(doc, tag, 'type-search-index.js');
    createElem(doc, tag, 'member-search-index.js');
    createElem(doc, tag, 'tag-search-index.js');
}

function createElem(doc, tag, path) {
    var script = doc.createElement(tag);
    var scriptElement = doc.getElementsByTagName(tag)[0];
    script.src = pathtoroot + path;
    scriptElement.parentNode.insertBefore(script, scriptElement);
}

function show(tableId, selected, columns) {
    if (tableId !== selected) {
        document.querySelectorAll('div.' + tableId + ':not(.' + selected + ')')
            .forEach(function(elem) {
                elem.style.display = 'none';
            });
    }
    document.querySelectorAll('div.' + selected)
        .forEach(function(elem, index) {
            elem.style.display = '';
            var isEvenRow = index % (columns * 2) < columns;
            elem.classList.remove(isEvenRow ? oddRowColor : evenRowColor);
            elem.classList.add(isEvenRow ? evenRowColor : oddRowColor);
        });
    updateTabs(tableId, selected);
}

function updateTabs(tableId, selected) {
    document.querySelector('div#' + tableId +' .summary-table')
        .setAttribute('aria-labelledby', selected);
    document.querySelectorAll('button[id^="' + tableId + '"]')
        .forEach(function(Editor_Tab, index) {
            if (selected === Editor_Tab.id || (tableId === selected && index === 0)) {
                Editor_Tab.className = activeTableTab;
                Editor_Tab.setAttribute('aria-selected', true);
                Editor_Tab.setAttribute('tabindex',0);
            } else {
                Editor_Tab.className = tableTab;
                Editor_Tab.setAttribute('aria-selected', false);
                Editor_Tab.setAttribute('tabindex',-1);
            }
        });
}

function switchTab(e) {
    var selected = document.querySelector('[aria-selected=true]');
    if (selected) {
        if ((e.keyCode === 37 || e.keyCode === 38) && selected.previousSibling) {
            // left or up arrow key pressed: move focus to previous Editor_Tab
            selected.previousSibling.click();
            selected.previousSibling.focus();
            e.preventDefault();
        } else if ((e.keyCode === 39 || e.keyCode === 40) && selected.nextSibling) {
            // right or down arrow key pressed: move focus to next Editor_Tab
            selected.nextSibling.click();
            selected.nextSibling.focus();
            e.preventDefault();
        }
    }
}

var updateSearchResults = function() {};

function indexFilesLoaded() {
    return moduleSearchIndex
        && packageSearchIndex
        && typeSearchIndex
        && memberSearchIndex
        && tagSearchIndex;
}

// Workaround for scroll position not being included in browser history (8249133)
document.addEventListener("DOMContentLoaded", function(e) {
    var contentDiv = document.querySelector("div.flex-content");
    window.addEventListener("popstate", function(e) {
        if (e.state !== null) {
            contentDiv.scrollTop = e.state;
        }
    });
    window.addEventListener("hashchange", function(e) {
        history.replaceState(contentDiv.scrollTop, document.title);
    });
    contentDiv.addEventListener("scroll", function(e) {
        var timeoutID;
        if (!timeoutID) {
            timeoutID = setTimeout(function() {
                history.replaceState(contentDiv.scrollTop, document.title);
                timeoutID = null;
            }, 100);
        }
    });
    if (!location.hash) {
        history.replaceState(contentDiv.scrollTop, document.title);
    }
});
