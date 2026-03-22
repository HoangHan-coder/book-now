import os
import re

def refactor_file(filepath, title="BookNow"):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        changed = False

        # 1. Replace HEAD
        # Preserve specific custom styles inside head
        head_pattern = re.compile(r'<head>.*?</head>', re.DOTALL)
        def head_replacer(match):
            head_text = match.group(0)
            
            # Extract anything inside <style> that isn't the generic scrollbar/gradient ones
            style_pattern = re.compile(r'<style>(.*?)</style>', re.DOTALL)
            styles = []
            for s in style_pattern.finditer(head_text):
                styles.append(s.group(1))
                
            combined_styles = ""
            for s in styles:
                # filter out generic parts 
                s = s.replace('::-webkit-scrollbar', '')
                # simplistic check to keep non-empty styles
                if len(s.strip()) > 50: 
                    combined_styles += s
            
            # Just keep the original <style> if there are custom ones, wait, the generic head
            # already has standard tailwind config + styles. 
            # We will just replace head completely but keep the style tags injected into body.
            
            style_inject = ""
            if "/* Hero Background */" in head_text or "/* Specific Logo" in head_text or ".hero-bg" in head_text:
                style_match = style_pattern.search(head_text)
                if style_match:
                    style_inject = "\n<style>" + style_match.group(1) + "</style>\n"

            return f'<head th:replace="~{{fragments/head :: head(title=\'{title}\')}}"></head>{style_inject}'

        new_content = head_pattern.sub(head_replacer, content)
        if new_content != content:
            changed = True
            content = new_content

        # Move the injected style from after </head> to inside <body>
        body_pattern = re.compile(r'(<head th:replace=".*?></head>)\s*(<style>.*?</style>)\s*(<body.*?>)', re.DOTALL)
        content = body_pattern.sub(r'\1\n\3\n\2', content)

        # 2. Replace NAVBAR (Public)
        # Matches the logo + nav block in index.html, home.html, DetailRoom (if standard)
        nav_pattern = re.compile(r'<!-- LOGO \(Fixed Position\) -->.*?</nav>', re.DOTALL)
        if nav_pattern.search(content):
            content = nav_pattern.sub('<div th:replace="~{fragments/navbar :: public-navbar}"></div>', content)
            changed = True
            
            # Clean up the old dropdown script if it exists
            script_pattern = re.compile(r'// Profile Dropdown Toggle.*?}\);', re.DOTALL)
            content = script_pattern.sub('', content)

        # 3. Replace FOOTER
        footer_pattern = re.compile(r'<!-- FOOTER -->\s*<footer class="bg-gray-900.*?>.*?</footer>', re.DOTALL)
        if footer_pattern.search(content):
            content = footer_pattern.sub('<footer th:replace="~{fragments/footer :: footer}"></footer>', content)
            changed = True

        # 4. Replace Admin/Staff Sidebar & Mobile Header & Fixed Logo
        # (Assuming they share the same structure)
        if 'id="sidebar"' in content and ('Bảng điều khiển' in content):
            # Admin Dashboard has href="/admin/dashboard" - Staff doesn't
            if '/admin/dashboard' in content:
                sidebar_frag = '<div th:replace="~{fragments/sidebar :: admin-sidebar(activePage=\'dashboard\')}"></div>'
            else:
                sidebar_frag = '<div th:replace="~{fragments/sidebar :: staff-sidebar(activePage=\'dashboard\')}"></div>'
            
            sidebar_pattern = re.compile(r'<!-- LOGO \(Fixed Position\) -->.*?<!-- OVERLAY \(for mobile\) -->.*?</div>', re.DOTALL)
            if sidebar_pattern.search(content):
                content = sidebar_pattern.sub(sidebar_frag, content)
                changed = True
                
                # Replace the sidebar script at bottom
                script_pattern = re.compile(r'const mobileMenuBtn = document\.getElementById\(\'mobileMenuBtn\'\);.*?sidebarOverlay\.addEventListener\(\'click\', toggleSidebar\);', re.DOTALL)
                content = script_pattern.sub('<div th:replace="~{fragments/sidebar :: sidebar-scripts}"></div>', content)

        if changed:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Refactored: {filepath}")

    except Exception as e:
        print(f"Error processing {filepath}: {e}")

# Apply to key files
base_dir = r"d:\Documents\FPT_Education\SP26\SWP391\project\book-now - Copy\src\main\resources\templates"
files_to_refactor = [
    "index.html",
    "public/home.html",
    "public/DetailRoom.html",
    "public/SearchRoom.html",
    "private/Admin_dashboard.html",
    "private/Staff_dashboard.html"
]

for f in files_to_refactor:
    full_path = os.path.join(base_dir, f.replace("/", "\\"))
    if os.path.exists(full_path):
        refactor_file(full_path, "BookNow")
    else:
        print(f"File not found: {full_path}")
