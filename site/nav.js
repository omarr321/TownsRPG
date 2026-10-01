(function () {
    var base = '/TownsRPG/';
    var links = [
        ['Home', base],
        ['Javadoc', base + 'javadoc/'],
        ['Guide', base + 'guide/'],
        ['GitHub', 'https://github.com/omarr321/TownsRPG']
    ];

    var css = `
    #site-nav { background:#0A0E1A; border-bottom:1px solid #39435C; position:sticky; top:0; z-index:1000;
                font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Helvetica,Arial,sans-serif; }
    #site-nav .inner { max-width:1200px; margin:0 auto; padding:0 20px; display:flex; align-items:center; gap:24px; height:56px; }
    #site-nav .brand { display:flex; align-items:center; gap:12px; margin-right:auto; text-decoration:none;
                       font-family:Georgia,"Times New Roman",serif; font-weight:700; font-size:20px; white-space:nowrap; }
    #site-nav .brand img { width:34px; height:34px; display:block; }
    #site-nav .brand .t { color:#C4C8D4; }
    #site-nav .brand .r { color:#D9822B; }
    #site-nav .brand .e { color:#8E94A8; font-weight:400; }
    #site-nav a.link { color:#8E94A8; text-decoration:none; font-size:15px; padding:17px 2px 15px; border-bottom:2px solid transparent; }
    #site-nav a.link:hover { color:#C4C8D4; }
    #site-nav a.link.active { color:#D9822B; border-bottom-color:#D9822B; }
    @media (max-width:560px) { #site-nav .brand span { display:none; } #site-nav .inner { gap:16px; } }
  `;

    function build() {
        var style = document.createElement('style');
        style.textContent = css;
        document.head.appendChild(style);

        if (!document.querySelector('link[rel~="icon"]')) {
            var icon = document.createElement('link');
            icon.rel = 'icon';
            icon.type = 'image/svg+xml';
            icon.href = base + 'branding/icon.svg';
            document.head.appendChild(icon);
        }

        var path = location.pathname;
        var html = '<div class="inner">'
            + '<a class="brand" href="' + base + '">'
            + '<img src="' + base + 'branding/icon.svg" alt="">'
            + '<span><span class="t">Towns </span><span class="r">RPG </span><span class="e">Engine</span></span></a>';

        links.forEach(function (l) {
            var active = l[1] === base
                ? (path === base || path === base + 'index.html')
                : path.indexOf(l[1]) === 0;
            html += '<a class="link' + (active ? ' active' : '') + '" href="' + l[1] + '">' + l[0] + '</a>';
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