#!/usr/bin/env python3
"""踩地雷 · 復古風格視覺提案產生器

用法:
    python docs/mockups/generate_mockups.py

產出 docs/mockups/ 內候選復古風格(經典版 / 復古掌機版 × 淺色 / 深色)的
390x844 SVG 視覺稿與 index.html 總覽。骨架座標沿用 gameboy-classic.svg /
gameboy-handheld.svg 的實測值,棋盤盤面所有風格共用。
PNG(780x1688)請另以 headless Chrome 匯出,見 docs/DEV_LOG.md。
"""

import html
import math
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
W, H = 390, 844
SANS = "'Microsoft JhengHei','Noto Sans TC','Segoe UI',sans-serif"
MONO = "'Courier New','Microsoft JhengHei',monospace"

# 9x9 展示盤面:# 未翻開 / F 旗幟 / M 地雷(翻開) / m 地雷(未翻開) / X 觸雷 / 1-8 數字
BOARD = [
    "##1F#111#",
    "##1111M1#",
    "#####111#",
    "###112F1#",
    "##12X311#",
    "#12F2M1##",
    "#1M1111##",
    "#111#####",
    "#########",
]

# 版型骨架(實測自既有 mockup)
BRAND_Y, TITLE_XY, SUB1_XY, SUB2_XY, FLAVOR_Y = 18, (16, 54), (16, 69), (16, 84), 100
ICON_XS, ICON_Y, ICON_S = (256, 296, 336), 30, 38
STATUS = (16, 112, 358, 68)
LBL1_XY, WELL1, DISP1, LBL2_XY, WELL2, DISP2 = (63, 128), (28, 135, 70, 32), (63, 158.04), (321, 128), (280, 135, 82, 32), (321, 158.04)
FACE = (173, 124, 44)
BOARD_XY, CELL0, CS = (16, 190, 358, 358), (24, 198), 38
BTNS, BTN_Y, BTN_H, BTN_TEXT_Y = ((16, 114, "插旗"), (138, 114, "置中"), (260, 114, "新遊戲")), 560, 52, 592.24
FOOT_Y = {"classic": 632, "handheld": 680}
PAD_A, PAD_B, PAD_R = (68, 594), (146, 594), 25
PAD_CAP_Y = 638
START, START_TEXT, START_CAP = (270, 579, 96, 30), (318, 597.9), (318, 624)

STYLE_NO = {
    "gameboy": ("01", "02"), "c64": ("03", "04"),
    "famicom": ("05", "06"), "apple2": ("07", "08"), "wingreen": ("09", "10"),
}


def n(v):
    s = f"{round(float(v), 2):.2f}".rstrip("0").rstrip(".")
    return s if s else "0"


def esc(s):
    return html.escape(s, quote=False)


def rect(x, y, w, h, fill, stroke=None, sw=1, rx=None, op=None):
    a = f'<rect x="{n(x)}" y="{n(y)}" width="{n(w)}" height="{n(h)}" fill="{fill}"'
    if stroke:
        a += f' stroke="{stroke}" stroke-width="{n(sw)}"'
    if rx:
        a += f' rx="{n(rx)}"'
    if op is not None:
        a += f' opacity="{n(op)}"'
    return a + "/>"


def line(x1, y1, x2, y2, stroke, sw):
    return (f'<line x1="{n(x1)}" y1="{n(y1)}" x2="{n(x2)}" y2="{n(y2)}" '
            f'stroke="{stroke}" stroke-width="{n(sw)}"/>')


def circle(cx, cy, r, fill="none", stroke=None, sw=1):
    a = f'<circle cx="{n(cx)}" cy="{n(cy)}" r="{n(r)}" fill="{fill}"'
    if stroke:
        a += f' stroke="{stroke}" stroke-width="{n(sw)}"'
    return a + "/>"


def path(d, fill="none", stroke=None, sw=1):
    a = f'<path d="{d}" fill="{fill}"'
    if stroke:
        a += f' stroke="{stroke}" stroke-width="{n(sw)}"'
    return a + "/>"


def text(x, y, s, fill, size, family=SANS, weight=700, anchor="start", ls=None):
    a = (f'<text x="{n(x)}" y="{n(y)}" fill="{fill}" font-family="{family}" '
         f'font-size="{n(size)}" font-weight="{weight}" text-anchor="{anchor}"')
    if ls:
        a += f' letter-spacing="{n(ls)}"'
    return a + f">{esc(s)}</text>"


# ---------- 邊框語言(每種風格指定一種) ----------

def border_bevel(p, x, y, w, h, bw, raised):
    out = [rect(x, y, w, h, p["panel"] if raised else p["well"])]
    hi, lo = (p["light"], p["dark"]) if raised else (p["dark"], p["light"])
    out += [rect(x, y, w, bw, hi), rect(x, y, bw, h, hi),
            rect(x, y + h - bw, w, bw, lo), rect(x + w - bw, y, bw, h, lo)]
    return out


def border_hardpixel(p, x, y, w, h, bw, raised):
    out = [rect(x, y, w, h, p["panel"] if raised else p["well"], p["frame"], 2)]
    hi, lo = (p["light"], p["dark"]) if raised else (p["dark"], p["light"])
    i = 2
    out += [rect(x + i, y + i, w - 2 * i, 1, hi), rect(x + i, y + i, 1, h - 2 * i, hi),
            rect(x + i, y + h - i - 1, w - 2 * i, 1, lo), rect(x + w - i - 1, y + i, 1, h - 2 * i, lo)]
    return out


def border_double(p, x, y, w, h, bw, raised):
    fill = p["panel"] if raised else p["well"]
    outer = p["frame"] if raised else p["frame2"]
    out = [rect(x, y, w, h, fill, outer, 2),
           rect(x + 4, y + 4, w - 8, h - 8, "none", p["frame"], 1)]
    return out


def border_dosdlg(p, x, y, w, h, bw, raised):
    out = [rect(x + 3, y + 3, w, h, p["shadow"])]
    out += [rect(x, y, w, h, p["panel"] if raised else p["well"],
                 p["frame"] if raised else p["light"], 1)]
    return out


def border_zxshadow(p, x, y, w, h, bw, raised):
    out = [rect(x + 3, y + 3, w, h, p["shadow"])]
    out += [rect(x, y, w, h, p["panel"] if raised else p["well"], p["frame"], 1)]
    return out


def border_flat(p, x, y, w, h, bw, raised):
    return [rect(x, y, w, h, p["panel"] if raised else p["well"], p["frame"], 1)]


BORDERS = {
    "bevel": border_bevel,        # Game Boy:像素斜邊(亮上左 / 暗下右)
    "hardpixel": border_hardpixel,  # Famicom:純黑硬描邊 + 1px 內高光
    "double": border_double,      # C64:PETSCII 雙線框
    "dosdlg": border_dosdlg,      # DOS:對話框 + 右下硬投影
    "zxshadow": border_zxshadow,  # ZX:1px 硬邊 + 右下硬陰影塊
    "flat": border_flat,          # Apple II:扁平 1px 外框
}


def frame(p, x, y, w, h, bw=3, raised=True):
    return BORDERS[p["border"]](p, x, y, w, h, bw, raised)


# ---------- 圖形字形 ----------

def glyph_mine(x0, y0, s, fill, hi):
    cx, cy, r = x0 + s / 2, y0 + s / 2, 0.2 * s
    sw = 0.07 * s
    out = [line(cx - 1.7 * r, cy, cx + 1.7 * r, cy, fill, sw),
           line(cx, cy - 1.7 * r, cx, cy + 1.7 * r, fill, sw)]
    d = 1.7 * r / math.sqrt(2)
    out += [line(cx - d, cy - d, cx + d, cy + d, fill, sw),
            line(cx - d, cy + d, cx + d, cy - d, fill, sw)]
    out = [circle(cx, cy, r, fill)] + out
    out.append(circle(cx - 0.07 * s, cy - 0.07 * s, 0.035 * s, hi))
    return out


def glyph_flag(x0, y0, s, color):
    sw = 0.07 * s
    return [
        line(x0 + 0.43 * s, y0 + 0.2 * s, x0 + 0.43 * s, y0 + 0.76 * s, color, sw),
        path(f"M{n(x0 + 0.43 * s)},{n(y0 + 0.2 * s)} L{n(x0 + 0.71 * s)},{n(y0 + 0.33 * s)} "
             f"L{n(x0 + 0.43 * s)},{n(y0 + 0.47 * s)} Z", fill=color),
        line(x0 + 0.28 * s, y0 + 0.78 * s, x0 + 0.72 * s, y0 + 0.78 * s, color, sw),
    ]


def glyph_face(x, y, s, color):
    cx, cy = x + s / 2, y + s / 2
    sw = 0.0455 * s
    return [
        circle(cx, cy, 0.26 * s, "none", color, sw),
        circle(cx - 0.09 * s, cy - 0.06 * s, 0.025 * s, color),
        circle(cx + 0.09 * s, cy - 0.06 * s, 0.025 * s, color),
        path(f"M{n(cx - 0.12 * s)},{n(cy + 0.08 * s)} Q{n(cx)},{n(cy + 0.22 * s)} "
             f"{n(cx + 0.12 * s)},{n(cy + 0.08 * s)}", stroke=color, sw=sw),
    ]


def glyph_clock(cx, cy, s, color):
    sw, r = 0.058 * s, 0.1764 * s
    return [circle(cx, cy, r, "none", color, sw),
            line(cx, cy, cx, cy - 0.105 * s, color, sw),
            line(cx, cy, cx + 0.0925 * s, cy + 0.0337 * s, color, sw)]


def glyph_fullscreen(cx, cy, s, color):
    sw, e, arm = 0.058 * s, 0.189 * s, 0.147 * s
    out = []
    for sx, sy in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        x1, y1 = cx + sx * e, cy + sy * (e - arm)
        x2, y2 = cx + sx * (e - arm), cy + sy * e
        out.append(path(f"M{n(x1)},{n(y1)} L{n(cx + sx * e)},{n(cy + sy * e)} L{n(x2)},{n(y2)}",
                        stroke=color, sw=sw))
    return out


def glyph_mine_icon(cx, cy, s, color):
    sw, r = 0.058 * s, 0.084 * s
    out = [circle(cx, cy, r, "none", color, sw)]
    for i in range(8):
        a = math.radians(i * 45)
        out.append(line(cx + 1.65 * r * math.cos(a), cy + 1.65 * r * math.sin(a),
                        cx + 2.5 * r * math.cos(a), cy + 2.5 * r * math.sin(a), color, sw))
    return out


# ---------- 版面元素 ----------

def draw_defs(p, sid):
    if p["pattern"] == "dots":
        return (f'<defs><pattern id="dots{sid}" width="4" height="4" patternUnits="userSpaceOnUse">'
                f'<circle cx="1" cy="1" r="0.55" fill="{p["patternDot"]}" '
                f'opacity="{n(p["patternOp"])}"/></pattern></defs>')
    if p["pattern"] == "scan":
        return (f'<defs><pattern id="scan{sid}" width="4" height="3" patternUnits="userSpaceOnUse">'
                f'<rect width="4" height="1" fill="#000000" opacity="{n(p["patternOp"])}"/>'
                f'</pattern></defs>')
    return "<defs></defs>"


def draw_background(p, sid):
    out = [rect(0, 0, W, H, p.get("tvBorder", p["desktop"]))]
    if "tvBorder" in p:
        out.append(rect(8, 8, W - 16, H - 16, p["desktop"]))
    if p["pattern"] == "dots":
        out.append(f'<rect x="0" y="0" width="{W}" height="{H}" fill="url(#dots{sid})"/>')
    return out


def draw_header(p, layout, mode):
    lay = "CLASSIC" if layout == "classic" else "HANDHELD"
    desktop_text = p.get("desktopText", p["text"])
    desktop_muted = p.get("desktopMuted", p["muted"])
    out = []
    if p.get("menubar"):
        out.append(rect(0, 0, W, 22, p["panel"]))
        out.append(line(0, 22, W, 22, p["frame"], 2))
        for x, s in ((12, "檔案"), (54, "編輯"), (96, "檢視"), (138, "說明")):
            out.append(text(x, 15, s, p["text"], 10))
        out.append(text(374, 15, f'{p["name_en"]}  /  {lay}', p["muted"], 8, weight=700, anchor="end"))
    else:
        out.append(text(16, BRAND_Y, f'{p["name_en"]}  /  {lay}', desktop_muted, 8, ls=1.2))
    out.append(text(*TITLE_XY, "踩地雷", desktop_text, 26, weight=900))
    out.append(text(*SUB1_XY, "MINESWEEPER", desktop_muted, 9, ls=1))
    out.append(text(*SUB2_XY, "初級 · 9 × 9 · 10 雷", desktop_muted, 9))
    out.append(text(16, FLAVOR_Y, p["flavor"], desktop_muted, 8, family=MONO if p.get("flavorMono") else SANS, ls=0.5))
    for i, x in enumerate(ICON_XS):
        out += frame(p, x, ICON_Y, ICON_S, ICON_S, 3)
        cx, cy = x + ICON_S / 2, ICON_Y + ICON_S / 2
        out += [glyph_clock, glyph_fullscreen, glyph_mine_icon][i](cx, cy, ICON_S, p["text"])
    return out


def draw_status(p):
    x, y, w, h = STATUS
    out = frame(p, x, y, w, h, 5)
    out.append(text(*LBL1_XY, "剩餘地雷", p["muted"], 9, anchor="middle"))
    out.append(text(*LBL2_XY, "時間", p["muted"], 9, anchor="middle"))
    for (wx, wy, ww, wh), (dx, dy), s in ((WELL1, DISP1, "010"), (WELL2, DISP2, "00:42")):
        out += frame(p, wx, wy, ww, wh, 3, raised=False)
        out.append(text(dx, dy, s, p["wellText"], 16.64, family=MONO, anchor="middle", ls=2))
    fx, fy, fs = FACE
    out += frame(p, fx, fy, fs, fs, 3)
    out += glyph_face(fx, fy, fs, p["text"])
    return out


def draw_board(p):
    px, py, pw, ph = BOARD_XY
    out = frame(p, px, py, pw, ph, 5)
    for r, rowstr in enumerate(BOARD):
        for c, ch in enumerate(rowstr):
            x0, y0 = CELL0[0] + c * CS, CELL0[1] + r * CS
            if ch == "#":
                out += frame(p, x0, y0, CS, CS, 3)
            elif ch == "F":
                out += frame(p, x0, y0, CS, CS, 3)
                out += glyph_flag(x0, y0, CS, p["flag"])
            elif ch == "m":
                out += frame(p, x0, y0, CS, CS, 3)
                out += glyph_mine(x0, y0, CS, p["mine"], p["mineHi"])
            elif ch == "M":
                out.append(rect(x0, y0, CS, CS, p["revealed"], p["revStroke"], 1))
                out += glyph_mine(x0, y0, CS, p["mine"], p["mineHi"])
            elif ch == "X":
                out.append(rect(x0, y0, CS, CS, p["explodedBg"], p["explodedBg"], 1))
                out += glyph_mine(x0, y0, CS, p["xMine"], p["xMineHi"])
            else:
                out.append(rect(x0, y0, CS, CS, p["revealed"], p["revStroke"], 1))
                out.append(text(x0 + CS / 2, y0 + 0.73 * CS, ch, p["num"][int(ch) - 1],
                                0.62 * CS, family=MONO, anchor="middle"))
    return out


def draw_controls(p, layout):
    out = []
    if layout == "classic":
        for index, (x, w, label) in enumerate(BTNS):
            selected = index == 0
            sp = dict(p)
            if selected:
                sp["panel"] = p["accentColor"]
            out += frame(sp, x, BTN_Y, w, BTN_H, 3)
            out.append(text(x + w / 2, BTN_TEXT_Y, ("✓ " if selected else "") + label,
                            p["onAccentColor"] if selected else p["text"], 13, anchor="middle"))
    else:
        for index, ((cx, cy), letter, cap) in enumerate(((PAD_A, "A", "插旗"), (PAD_B, "B", "置中"))):
            selected = index == 0
            out.append(circle(cx, cy, PAD_R, p["accentColor"] if selected else p["ctrlFill"], p["ctrlStroke"], 3))
            out.append(text(cx, cy + 7, letter, p["onAccentColor"] if selected else p["ctrlText"], 19, family=MONO, anchor="middle"))
            out.append(text(cx, PAD_CAP_Y, ("✓ " if selected else "") + cap,
                            p["accentColor"] if selected else p["text"], 10, anchor="middle"))
        sx, sy, sw_, sh = START
        out.append(rect(sx, sy, sw_, sh, p["ctrlFill"], p["ctrlStroke"], 2, rx=sh / 2))
        out.append(text(*START_TEXT, "START", p["ctrlText"], 10, family=MONO, anchor="middle"))
        out.append(text(*START_CAP, "新遊戲", p["text"], 10, anchor="middle"))
    return out


def draw_footer(p, layout, mode):
    no = STYLE_NO[p["slug"]][0 if layout == "classic" else 1]
    name = f'STYLE {no} · {p["name_en"]}' + (" · DARK" if mode == "dark" else "")
    muted = p.get("desktopMuted", p["muted"])
    return [text(16, FOOT_Y[layout], name, muted, 9, ls=0.5),
            text(374, FOOT_Y[layout], "操作位置不變", muted, 9, weight=400, anchor="end")]


def draw_chrome(p):
    out = []
    if p["border"] == "zxshadow":
        y = H - 8
        for i, col in enumerate(p["rainbow"]):
            out.append(rect(i * W / 6, y, W / 6, 8, col))
    return out


# ---------- 風格設定檔 ----------

def pal(**kw):
    base = dict(border="bevel", pattern=None, patternOp=0.16, patternDot="#000000")
    base.update(kw)
    return base


STYLES = [
    {
        "slug": "gameboy",
        "accent": ("#306230", "#2A4A18"), "onAccent": ("#D8E8A3", "#B7E36B"),
        "name_en": "GAME BOY GREEN",
        "name_zh": "Game Boy 綠",
        "desc": "DMG 四色綠、點陣背景與像素斜邊，掌機螢幕質感。",
        "flavor": "NINTENDO GAME BOY · DOT MATRIX WITH STEREO SOUND",
        "chips": ["desktop", "panel", "light", "dark", "well", "text", "flag", "explodedBg"],
        "light": pal(
            border="bevel", pattern="dots", patternOp=0.16, patternDot="#0F380F",
            desktop="#9BBC0F", panel="#9BBC0F", light="#D8E8A3", dark="#306230",
            frame="#306230", frame2="#0F380F", shadow="#306230",
            well="#0F380F", wellText="#9BBC0F", text="#0F380F", muted="#306230",
            revealed="#8BAC0F", revStroke="#306230",
            flag="#0F380F", explodedBg="#306230",
            mine="#0F380F", mineHi="#FFFFFF", xMine="#9BBC0F", xMineHi="#306230",
            ctrlFill="#306230", ctrlStroke="#D8E8A3", ctrlText="#9BBC0F",
            num=["#0F380F", "#306230", "#0F380F", "#306230", "#0F380F", "#306230", "#0F380F", "#306230"],
        ),
        "dark": pal(
            border="bevel", pattern="dots", patternOp=0.18, patternDot="#000000",
            desktop="#0E1B0C", panel="#16290F", light="#4E7A2E", dark="#050D04",
            frame="#050D04", frame2="#0A150A", shadow="#050D04",
            well="#050D04", wellText="#A5D66A", text="#B7E36B", muted="#7FA64E",
            revealed="#1D3A14", revStroke="#050D04",
            flag="#CDEF8A", explodedBg="#3A5A18",
            mine="#B7E36B", mineHi="#050D04", xMine="#050D04", xMineHi="#7FA64E",
            ctrlFill="#2A4A18", ctrlStroke="#4E7A2E", ctrlText="#B7E36B",
            num=["#B7E36B", "#7FA64E", "#B7E36B", "#7FA64E", "#B7E36B", "#7FA64E", "#B7E36B", "#7FA64E"],
        ),
    },
    {
        "slug": "c64",
        "accent": ("#B8C76F", "#B8C76F"), "onAccent": ("#1D1542", "#1D1542"),
        "name_en": "COMMODORE 64",
        "name_zh": "Commodore 64",
        "desc": "VIC-II 藍底淺藍字、PETSCII 雙線框與電視邊框，家用電腦的靈魂。",
        "flavor": "64K RAM SYSTEM · 38911 BASIC BYTES FREE  READY.",
        "flavorMono": True,
        "chips": ["tvBorder", "desktop", "panel", "frame", "well", "text", "flag", "explodedBg"],
        "light": pal(
            border="double",
            tvBorder="#6C5EB5", desktop="#352879", panel="#352879",
            light="#7B71B2", dark="#1D1542", frame="#6C5EB5", frame2="#1D1542", shadow="#1D1542",
            well="#1D1542", wellText="#B8C76F", text="#B1A7E3", muted="#959595",
            revealed="#2B2062", revStroke="#6C5EB5",
            flag="#B8C76F", explodedBg="#9A6759",
            mine="#B1A7E3", mineHi="#FFFFFF", xMine="#1D1542", xMineHi="#B8C76F",
            ctrlFill="#1D1542", ctrlStroke="#6C5EB5", ctrlText="#B8C76F",
            num=["#9E95E8", "#9AD284", "#B98A7C", "#B1A7E3", "#B8C76F", "#70A4B2", "#FFFFFF", "#959595"],
        ),
        "dark": pal(
            border="double",
            tvBorder="#3B3480", desktop="#241B4E", panel="#241B4E",
            light="#4A3E96", dark="#120C28", frame="#4A3E96", frame2="#120C28", shadow="#120C28",
            well="#120C28", wellText="#B8C76F", text="#B1A7E3", muted="#7B71B2",
            revealed="#1D1542", revStroke="#4A3E96",
            flag="#B8C76F", explodedBg="#6F3D86",
            mine="#B1A7E3", mineHi="#FFFFFF", xMine="#120C28", xMineHi="#B8C76F",
            ctrlFill="#120C28", ctrlStroke="#4A3E96", ctrlText="#B8C76F",
            num=["#7B71B2", "#9AD284", "#9A6759", "#B1A7E3", "#B8C76F", "#70A4B2", "#E8E8E8", "#959595"],
        ),
    },
    {
        "slug": "famicom",
        "accent": ("#D82800", "#F83800"), "onAccent": ("#FCFCFC", "#FCFCFC"),
        "name_en": "FAMICOM",
        "name_zh": "紅白機 Famicom",
        "desc": "奶油機殼配 NES 紅，2px 純黑硬描邊與像素高光，卡帶時代的俐落感。",
        "flavor": "© 1983 MINESWEEPER · 1P START",
        "chips": ["desktop", "panel", "light", "dark", "well", "text", "flag", "explodedBg"],
        "light": pal(
            border="hardpixel",
            desktop="#EDE4C5", panel="#FCFCFC", light="#FFFFFF", dark="#A8A4A0",
            frame="#000000", frame2="#7B7054", shadow="#7B7054",
            well="#000000", wellText="#FCFCFC", text="#101010", muted="#7B7054",
            revealed="#DCD6C4", revStroke="#101010",
            flag="#D82800", explodedBg="#D82800",
            mine="#101010", mineHi="#FCFCFC", xMine="#000000", xMineHi="#FCFCFC",
            ctrlFill="#B53120", ctrlStroke="#000000", ctrlText="#FCFCFC",
            num=["#0058F8", "#008400", "#D82800", "#0038A8", "#881400", "#007878", "#101010", "#6C6C6C"],
        ),
        "dark": pal(
            border="hardpixel",
            desktop="#241E14", panel="#332B1E", light="#57503C", dark="#12100A",
            frame="#EDE4C5", frame2="#12100A", shadow="#12100A",
            well="#000000", wellText="#FCFCFC", text="#EDE4C5", muted="#A89870",
            revealed="#2A2418", revStroke="#8A7E5C",
            flag="#F83800", explodedBg="#F83800",
            mine="#EDE4C5", mineHi="#241E14", xMine="#000000", xMineHi="#FCFCFC",
            ctrlFill="#D82800", ctrlStroke="#000000", ctrlText="#FCFCFC",
            num=["#3CBCFC", "#58D854", "#F83800", "#6C8CF8", "#F87858", "#3CE0D0", "#FCFCFC", "#BCBCBC"],
        ),
    },
    {
        "slug": "apple2",
        "accent": ("#4CFF5E", "#35C24A"), "onAccent": ("#0A220A", "#030B03"),
        "name_en": "APPLE II GREEN",
        "name_zh": "Apple II 綠幕",
        "desc": "P1 磷光綠單色、掃描線紋理與扁平外框，終端機時代的綠色螢幕。",
        "flavor": "APPLE ][ MINESWEEPER · PRESS ANY KEY",
        "flavorMono": True,
        "allMono": True,
        "chips": ["desktop", "panel", "frame", "well", "text", "flag", "explodedBg", "revealed"],
        "light": pal(
            border="flat", pattern="scan", patternOp=0.35,
            desktop="#0A220A", panel="#0A220A", light="#4CFF5E", dark="#031403",
            frame="#2E9E3F", frame2="#031403", shadow="#031403",
            well="#031403", wellText="#4CFF5E", text="#4CFF5E", muted="#2E9E3F",
            revealed="#0F330F", revStroke="#2E9E3F",
            flag="#4CFF5E", explodedBg="#2E9E3F",
            mine="#4CFF5E", mineHi="#0A220A", xMine="#0A220A", xMineHi="#4CFF5E",
            ctrlFill="#0F330F", ctrlStroke="#4CFF5E", ctrlText="#4CFF5E",
            num=["#4CFF5E", "#2E9E3F", "#4CFF5E", "#2E9E3F", "#4CFF5E", "#2E9E3F", "#4CFF5E", "#2E9E3F"],
        ),
        "dark": pal(
            border="flat", pattern="scan", patternOp=0.45,
            desktop="#030B03", panel="#030B03", light="#35C24A", dark="#010501",
            frame="#1E7A2E", frame2="#010501", shadow="#010501",
            well="#010501", wellText="#2E9E3F", text="#35C24A", muted="#1E7A2E",
            revealed="#081C08", revStroke="#1E7A2E",
            flag="#35C24A", explodedBg="#1E7A2E",
            mine="#35C24A", mineHi="#030B03", xMine="#030B03", xMineHi="#35C24A",
            ctrlFill="#081C08", ctrlStroke="#2E9E3F", ctrlText="#35C24A",
            num=["#35C24A", "#1E7A2E", "#35C24A", "#1E7A2E", "#35C24A", "#1E7A2E", "#35C24A", "#1E7A2E"],
        ),
    },
    {
        "slug": "wingreen",
        "accent": ("#0F7A52", "#2E8C64"), "onAccent": ("#FFFFFF", "#FFFFFF"),
        "name_en": "WINDOWS GRAY GREEN",
        "name_zh": "Windows 灰綠",
        "desc": "Windows 灰搭配經典綠選取色，Win95 換色版。",
        "flavor": "WINDOWS STANDARD · SPRUCE GREEN",
        "chips": ["desktop", "panel", "light", "dark", "well", "text", "flag", "explodedBg"],
        "light": pal(
            border="bevel",
            desktop="#D8D8D8", panel="#C0C0C0",
            light="#FFFFFF", dark="#707070", frame="#171717", frame2="#707070", shadow="#707070",
            well="#000000", wellText="#FF3333", text="#171717", muted="#555555",
            desktopText="#171717", desktopMuted="#555555",
            revealed="#C0C0C0", revStroke="#707070",
            flag="#FF2020", explodedBg="#FF5555",
            mine="#171717", mineHi="#FFFFFF", xMine="#171717", xMineHi="#FFFFFF",
            ctrlFill="#707070", ctrlStroke="#FFFFFF", ctrlText="#FFFFFF",
            num=["#0000C0", "#006400", "#D00000", "#000080", "#800000", "#006060", "#101010", "#606060"],
        ),
        "dark": pal(
            border="bevel",
            desktop="#15171C", panel="#3A3D45",
            light="#686C76", dark="#16181D", frame="#F5F5F5", frame2="#16181D", shadow="#16181D",
            well="#08090B", wellText="#FF3333", text="#F5F5F5", muted="#B8BBC4",
            desktopText="#F5F5F5", desktopMuted="#B8BBC4",
            revealed="#3A3D45", revStroke="#16181D",
            flag="#FF2020", explodedBg="#FF5555",
            mine="#F5F5F5", mineHi="#FFFFFF", xMine="#F5F5F5", xMineHi="#FFFFFF",
            ctrlFill="#16181D", ctrlStroke="#686C76", ctrlText="#686C76",
            num=["#7C9CFF", "#3FBF5F", "#FF6060", "#9A9AFF", "#E07070", "#40C0C0", "#F0F0F0", "#B0B0B0"],
        ),
    },
]

def build_svg(style, layout, mode):
    p = dict(style[mode])
    p.update({k: v for k, v in style.items() if k not in ("light", "dark", "accent", "onAccent")})
    mode_index = 0 if mode == "light" else 1
    p["accentColor"] = style["accent"][mode_index]
    p["onAccentColor"] = style["onAccent"][mode_index]
    lay_zh = "經典版" if layout == "classic" else "復古掌機版"
    title = f'{style["name_en"]} · {lay_zh}' + (" · 深色" if mode == "dark" else "")
    sid = f'{style["slug"]}{layout}{mode}'
    fam = MONO if style.get("allMono") else SANS

    body = []
    body += draw_background(p, sid)
    body += draw_header(p, layout, mode)
    body += draw_status(p)
    body += draw_board(p)
    body += draw_controls(p, layout)
    body += draw_footer(p, layout, mode)
    body += draw_chrome(p)
    if p["pattern"] == "scan":
        body.append(f'<rect x="0" y="0" width="{W}" height="{H}" fill="url(#scan{sid})"/>')

    if fam == MONO:
        joined = "\n".join(body).replace(f'font-family="{SANS}"', f'font-family="{MONO}"')
    else:
        joined = "\n".join(body)

    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" '
            f'viewBox="0 0 {W} {H}" role="img" aria-label="{esc(title)}">\n'
            f'<title>{esc(title)}</title>\n{draw_defs(p, sid)}\n{joined}\n</svg>\n')


CHIP_LABELS = {
    "desktop": "底色", "panel": "面板", "light": "亮邊", "dark": "暗邊",
    "frame": "框線", "shadow": "陰影", "well": "顯示井", "wellText": "顯示字",
    "text": "文字", "flag": "旗標", "explodedBg": "觸雷格", "revealed": "翻開格",
    "tvBorder": "電視邊框",
}


def sel_chip(accent: str, on_accent: str) -> str:
    return (f'<span class="chip"><i style="background:{accent}"></i>選取 <b>{accent}</b></span>'
            f'<span class="chip"><i style="background:{on_accent}"></i>選取字 <b>{on_accent}</b></span>')


def chip_html(p, keys):
    out = []
    for k in keys:
        v = p.get(k)
        if not v:
            continue
        out.append(f'<span class="chip"><i style="background:{v}"></i>{CHIP_LABELS.get(k, k)} '
                   f'<b>{v}</b></span>')
    return "\n        ".join(out)


def build_index():
    cards = []
    seq = 1
    for st in STYLES:
        slug = st["slug"]
        imgs = []
        for layout, lay_zh in (("classic", "經典版"), ("handheld", "復古掌機版")):
            for mode, mode_zh in (("light", "淺色"), ("dark", "深色")):
                suffix = "-dark" if mode == "dark" else ""
                src = f"{slug}-{layout}{suffix}.svg"
                no = STYLE_NO[slug][0 if layout == "classic" else 1]
                imgs.append(f'''<figure>
          <div class="frame"><img src="{src}" alt="{esc(st["name_zh"])} × {lay_zh} · {mode_zh}"></div>
          <figcaption>STYLE {no} · {lay_zh} · {mode_zh}</figcaption>
        </figure>''')
        cards.append(f'''<section class="style">
      <h2>STYLE {STYLE_NO[slug][0]} · {esc(st["name_zh"])}</h2>
      <p>{esc(st["desc"])}</p>
      <div class="pal">
        <div class="palrow"><span class="paltag">淺色</span>{chip_html(st["light"], st["chips"])}
        {sel_chip(st["accent"][0], st["onAccent"][0])}</div>
        <div class="palrow"><span class="paltag">深色</span>{chip_html(st["dark"], st["chips"])}
        {sel_chip(st["accent"][1], st["onAccent"][1])}</div>
      </div>
      <div class="shots">{" ".join(imgs)}</div>
    </section>''')
        seq += 1

    mac = '''<section class="style ref">
      <h2>參考 · 麥金塔單色（已實作於 1.4.0）</h2>
      <p>System 7 黑白視窗、條紋標題列、單色數字與旗幟，固定黑白配色，不在本次挑選範圍。</p>
      <div class="shots">
        <figure>
          <div class="frame"><img src="macintosh-classic.svg" alt="麥金塔單色 × 經典版"></div>
          <figcaption>經典版</figcaption>
        </figure>
        <figure>
          <div class="frame"><img src="macintosh-handheld.svg" alt="麥金塔單色 × 復古掌機版"></div>
          <figcaption>復古掌機版</figcaption>
        </figure>
      </div>
    </section>'''

    return f'''<!doctype html>
<html lang="zh-Hant">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>踩地雷 · 復古風格視覺提案</title>
<style>
  :root {{ color-scheme: dark; }}
  * {{ box-sizing: border-box; }}
  body {{ margin: 0; padding: 32px; background: #15171c; color: #f5f5f5; font-family: 'Microsoft JhengHei','Noto Sans TC','Segoe UI',sans-serif; }}
  h1 {{ margin: 0 0 6px; font-size: 26px; }}
  p.lead {{ margin: 0 0 28px; color: #b8bbc4; }}
  .style {{ max-width: 980px; margin: 0 0 34px; background: #22252b; border: 1px solid #3a3d45; border-radius: 14px; padding: 18px; }}
  .style h2 {{ margin: 0 0 4px; font-size: 18px; }}
  .style > p {{ margin: 0 0 12px; color: #b8bbc4; font-size: 13px; line-height: 1.6; }}
  .style.ref {{ opacity: 0.75; }}
  .pal {{ margin: 0 0 14px; font-size: 11px; }}
  .palrow {{ display: flex; flex-wrap: wrap; gap: 8px; align-items: center; margin: 6px 0; }}
  .paltag {{ width: 42px; color: #b8bbc4; }}
  .chip {{ display: inline-flex; align-items: center; gap: 5px; padding: 3px 8px; border: 1px solid #4a4e57; border-radius: 999px; color: #b8bbc4; }}
  .chip i {{ width: 12px; height: 12px; border-radius: 3px; border: 1px solid #0006; }}
  .chip b {{ color: #f5f5f5; font-weight: 600; }}
  .shots {{ display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }}
  figure {{ margin: 0; }}
  .frame {{ border-radius: 10px; overflow: hidden; background: #0f1014; }}
  img {{ display: block; width: 100%; height: auto; }}
  figcaption {{ margin-top: 6px; color: #b8bbc4; font-size: 11px; }}
  .note {{ max-width: 980px; margin-top: 10px; padding: 16px; border-left: 3px solid #8bac0f; background: #1b1e23; color: #c9ccd3; line-height: 1.6; }}
</style>
</head>
<body>
  <h1>踩地雷 · 復古風格視覺提案</h1>
  <p class="lead">復古風格視覺稿 ×（經典版 / 復古掌機版）×（淺色 / 深色）。控制按鈕位置不變，只改變配色、邊框與質感語言。本批視覺提案已全數實作於 App 1.5.0。</p>
  {"".join(cards)}
  {mac}
  <div class="note">
    <strong>共同原則：</strong>不改動棋盤、狀態列與操作按鈕的位置；只調整色盤、邊框、顯示器與字體氣質。
    每種風格均含淺色／深色變體。本批視覺提案已全數實作於 App（1.5.0）；
    SVG 由 <code>generate_mockups.py</code> 產生，可重跑微調。
  </div>
</body>
</html>
'''


def main():
    os.makedirs(HERE, exist_ok=True)
    made = 0
    for st in STYLES:
        for layout in ("classic", "handheld"):
            for mode in ("light", "dark"):
                suffix = "-dark" if mode == "dark" else ""
                fname = f'{st["slug"]}-{layout}{suffix}.svg'
                fpath = os.path.join(HERE, fname)
                with open(fpath, "w", encoding="utf-8", newline="\n") as f:
                    f.write(build_svg(st, layout, mode))
                made += 1
                print(f"write {fname}")
    with open(os.path.join(HERE, "index.html"), "w", encoding="utf-8", newline="\n") as f:
        f.write(build_index())
    print(f"write index.html({made} SVG)")


if __name__ == "__main__":
    main()
