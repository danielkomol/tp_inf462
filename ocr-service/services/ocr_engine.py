"""
SERVICE OCR — Extraction de texte depuis des images
=====================================================
Utilise Tesseract OCR (moteur open-source de reconnaissance
optique de caractères) pour extraire le texte de documents
scannés (CNI, bulletins de salaire, etc).

Tesseract fonctionne en 2 étapes :
1. Pré-traitement de l'image (amélioration de la qualité)
2. Reconnaissance de caractères (extraction du texte)
"""

import pytesseract
from PIL import Image
import cv2
import numpy as np
import re
from typing import Dict, Any


def preprocesser_image(image_path: str) -> np.ndarray:
    """
    PRÉ-TRAITEMENT DE L'IMAGE
    Améliore la qualité de l'image avant l'OCR pour
    augmenter la précision de l'extraction.

    Étapes :
    1. Conversion en niveaux de gris
    2. Suppression du bruit
    3. Binarisation (noir et blanc pur)
    """
    # Charger l'image avec OpenCV
    image = cv2.imread(image_path)

    # 1. Conversion en niveaux de gris
    # Les couleurs ne servent à rien pour l'OCR, ça simplifie le travail
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # 2. Suppression du bruit (flou gaussien léger)
    denoised = cv2.GaussianBlur(gray, (5, 5), 0)

    # 3. Binarisation avec la méthode d'Otsu
    # Transforme l'image en noir et blanc pur (meilleur contraste pour l'OCR)
    _, binary = cv2.threshold(
        denoised, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU
    )

    return binary


def extraire_texte(image_path: str, langue: str = "fra") -> Dict[str, Any]:
    """
    EXTRAIRE LE TEXTE D'UNE IMAGE

    Args:
        image_path: chemin vers l'image à analyser
        langue: langue du document ("fra" = français, "eng" = anglais)

    Returns:
        dict contenant le texte extrait et le niveau de confiance
    """
    # 1. Pré-traiter l'image pour améliorer la qualité
    image_traitee = preprocesser_image(image_path)

    # 2. Extraire le texte avec Tesseract
    texte_brut = pytesseract.image_to_string(image_traitee, lang=langue)

    # 3. Obtenir les données détaillées (avec niveau de confiance)
    donnees = pytesseract.image_to_data(
        image_traitee, lang=langue, output_type=pytesseract.Output.DICT
    )

    # 4. Calculer le niveau de confiance moyen
    # Tesseract retourne -1 pour les mots non détectés, on les ignore
    confidences = [int(c) for c in donnees["conf"] if int(c) > 0]
    confiance_moyenne = sum(confidences) / len(confidences) if confidences else 0

    return {
        "texte_brut": texte_brut.strip(),
        "niveau_confiance": round(confiance_moyenne, 2),
        "nombre_mots_detectes": len(confidences)
    }


def extraire_infos_cni(texte: str) -> Dict[str, Any]:
    """
    EXTRAIRE LES INFORMATIONS STRUCTURÉES D'UNE CNI
    Utilise des expressions régulières (regex) pour identifier
    les champs importants dans le texte brut extrait.

    NOTE: Ces patterns sont simplifiés pour la démo.
    En production, il faudrait les adapter au format exact
    des CNI camerounaises.
    """
    infos = {
        "nom": None,
        "prenom": None,
        "date_naissance": None,
        "numero_cni": None,
        "lieu_naissance": None
    }

    # Recherche du numéro de CNI (format: lettres + chiffres)
    match_numero = re.search(r'\b[A-Z]{0,2}\d{6,12}\b', texte)
    if match_numero:
        infos["numero_cni"] = match_numero.group()

    # Recherche d'une date au format JJ/MM/AAAA ou JJ-MM-AAAA
    match_date = re.search(r'\b(\d{2})[/-](\d{2})[/-](\d{4})\b', texte)
    if match_date:
        infos["date_naissance"] = match_date.group()

    # Recherche du nom (après le mot "NOM" ou "SURNAME")
    match_nom = re.search(r'(?:NOM|SURNAME)[:\s]+([A-Z]+)', texte, re.IGNORECASE)
    if match_nom:
        infos["nom"] = match_nom.group(1)

    # Recherche du prénom
    match_prenom = re.search(r'(?:PRENOM|GIVEN NAME)[:\s]+([A-Z\s]+)', texte, re.IGNORECASE)
    if match_prenom:
        infos["prenom"] = match_prenom.group(1).strip()

    return infos


def extraire_infos_bulletin_salaire(texte: str) -> Dict[str, Any]:
    """
    EXTRAIRE LES INFORMATIONS D'UN BULLETIN DE SALAIRE
    """
    infos = {
        "salaire_net": None,
        "salaire_brut": None,
        "employeur": None,
        "periode": None
    }

    # Recherche de montants (format avec espaces ou virgules pour les milliers)
    montants = re.findall(r'\b\d{1,3}(?:[\s,]\d{3})*(?:\.\d{2})?\b', texte)
    if montants:
        # On prend le montant le plus élevé comme salaire brut estimé
        montants_nums = [float(m.replace(' ', '').replace(',', '')) for m in montants]
        if montants_nums:
            infos["salaire_brut"] = max(montants_nums)

    return infos
