(function () {
    var base = '/TownsRPG/';
    var links = [
        ['Home', base],
        ['Javadoc', base + 'javadoc/'],
        ['Guide', base + 'guide/'],
        ['GitHub', 'https://github.com/omarr321/TownsRPG']
    ];

    var css = `
    #site-nav { background:#161b22; border-bottom:1px solid #30363d; font-family:-apple-system,"Segoe UI",Helvetica,Arial,sans-serif; }
    #site-nav .inner { max-width:1200px; margin:0 auto; padding:0 20px; display:flex; align-items:center; gap:24px; height:48px; }
    #site-nav .brand { font-weight:700; color:#e6edf3; margin-right:auto; text-decoration:none; }
    #site-nav a { color:#8d96a0; text-decoration:none; font-size:15px; }
    #site-nav a:hover, #site-nav a.active { color:#4493f8; }
  `;

    function build() {
        var style = document.createElement('style');
        style.textContent = css;
        document.head.appendChild(style);

        var path = location.pathname;
        var html = '<div class="inner"><a class="brand" href="' + base + '">TownsRPG</a>';
        links.forEach(function (l) {
            var active = l[1] === base
                ? (path === base || path === base + 'index.html')
                : path.indexOf(l[1]) === 0;
            html += '<a href="' + l[1] + '"' + (active ? ' class="active"' : '') + '>' + l[0] + '</a>';
        });
        html += '</div>';

        var nav = document.createElement('nav');
        nav.id = 'site-nav';
        nav.innerHTML = html;
        document.body.prepend(nav);
    }

    if (document.body) build();
    else document.addEventListener('DOMContentLoaded', build);
})();