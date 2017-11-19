<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:hptrans="http://www.hp.com/translations"
	xmlns:proj="http://www.hp.com/cdsplus" exclude-result-prefixes="hptrans  proj">
	<xsl:import href="blossom1.xsl" />
	<xsl:param name="note" select="'NOTE'" />
	<xsl:param name="caution" select="'CAUTION'" />
    <xsl:param name="warning" select="'WARNING'" />
	<xsl:param name="figure" select="'FIGURE'" />
	<xsl:param name="issue" select="'ISSUE'" />
	<xsl:param name="solution" select="'SOLUTION'" />
	<xsl:param name="cause" select="'CAUSE'" />
	<xsl:param name="workaround" select="'WORKAROUND'" />
	<xsl:param name="summary" select="'SUMMARY'" />
	<xsl:param name="question" select="'QUESTION'" />
	<xsl:param name="answer" select="'ANSWER'" />
	<xsl:param name="standalone_document" select="true()" />
	<xsl:param name="on-line_delivery" select="true()" />
	<xsl:param name="printable" select="'no'"/>
	<xsl:param name="corporate_style" select="'hp'"/>
	
	<xsl:template match="*">
		<xsl:call-template name="build_doc" />
	</xsl:template>
	<xsl:template name="build_doc">
		<xsl:choose>
			<xsl:when test="$standalone_document = 'true'">
				<xsl:text disable-output-escaping="yes">&#060;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"&#062;</xsl:text>
				<html>
					<head>
						<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
						<title>
							<xsl:value-of select="//c_support_doc/title" />
						</title>
						<xsl:choose>
							<xsl:when test="$on-line_delivery = 'true'">
								<script type="text/javascript" language="JavaScript">
									&lt;!--
									<xsl:choose>
										<xsl:when test="$corporate_style = 'compaq'">
											var theme = '#FF0000';
										</xsl:when>
										<xsl:otherwise>
											var theme = '#CC0066';
										</xsl:otherwise>
									</xsl:choose>
									//-->
								</script>
								<script type="text/javascript" language="JavaScript"
									src="http://welcome.hp-ww.com/country/us/en/js/hpweb_utilities.js">&amp;nbsp;</script>
							</xsl:when>
							<xsl:otherwise>
								<style type="text/css">
									body,td,th {font-family: Arial, Verdana,
									Helvetica, Sans-serif;
									font-size: 12px;}
									a {color: #003366;
									text-decoration: none;}
									a:active {color: #003366;}
									a:link {color:
									#003366;}
									a:visited {color: #660066;}
									a:hover {text-decoration:
									underline;}

									h1 {font-size: 25px; font-weight: normal;
									margin-bottom: 2px;
									margin-top: 2px;}
									h2 h3 h4 h5 h6 {font-size:
									12px; margin-bottom: 1px; margin-top:
									1px; font-weight: normal;}

									.themeheader {color:#FFFFFF; font-weight:bold;}
									.themebody
									{color:#FFFFFF;}
									a.themeheaderlink {font-weight: bold;
									color:#FFFFFF; text-decoration: none;}
									a.themeheaderlink:active
									{font-weight: bold; color:#FFFFFF;}
									a.themeheaderlink:link
									{font-weight: bold; color:#FFFFFF;}
									a.themeheaderlink:visited
									{font-weight: bold; color:#FFFFFF;}
									a.themeheaderlink:hover
									{text-decoration: underline;}
									a.themelink {color:#FFFFFF;
									text-decoration: none;}
									a.themelink:active {color:#FFFFFF;}
									a.themelink:link {color:#FFFFFF;}
									a.themelink:visited
									{color:#FFFFFF;}
									a.themelink:hover {text-decoration: underline;}
									a.themebodylink {color:#FFFFFF; text-decoration: underline;}
									a.themebodylink:active {color:#FFFFFF;}
									a.themebodylink:link
									{color:#FFFFFF;}
									a.themebodylink:visited {color:#FFFFFF;}
									.theme
									{background: #0066FF}

								</style>
							</xsl:otherwise>
						</xsl:choose>
					</head>
					<body>
						<div style="width: 560px;">
							<h1>
								<xsl:value-of select="title" />
							</h1>
							<xsl:apply-templates mode="concentra_doc" />
						</div>
					</body>
				</html>
			</xsl:when>
			<xsl:otherwise>
				<div style="width: 560px;">
					<xsl:apply-templates mode="concentra_doc" />
				</div>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="properties" mode="concentra_doc">
		<div class="copyright.notice" style="margin-top: 1ex; margin-bottom: 1ex;">
			<p
				style="margin-top: 0pt; margin-bottom: 1ex; font-size: 80%; text-align: center;">©Copyright 2007 Hewlett-Packard Development Company, L.P. </p>
			<p
				style="padding: 0pt; margin-top: 0pt; margin-bottom: 2ex; font-size: 80%; text-align: justify;">
				Hewlett-Packard Company shall not be liable for technical or
				editorial errors or
				omissions
				contained herein. The information
				provided is provided "as is"
				without warranty of any kind. To the
				extent permitted by law, neither HP or
				its affiliates, subcontractors
				or suppliers will be liable for
				incidental,
				special or consequential
				damages including downtime cost; lost profits;
				damages relating to
				the procurement of substitute products or
				services; or
				damages for
				loss of data, or software restoration. The information in this
				document is subject to change without notice. Hewlett-Packard
				Company and the
				names of Hewlett-Packard products referenced herein
				are trademarks of
				Hewlett-Packard Company in the United States and
				other countries.
				Other product
				and company names mentioned herein may
				be trademarks of their
				respective
				owners.</p>
		</div>
	</xsl:template>
	<xsl:template match="link" mode="concentra_doc">
		<xsl:text>&#xA;</xsl:text>
		<xsl:choose>
			<xsl:when test="$printable = 'no'">
				<xsl:element name="a">
					<xsl:choose>
						<xsl:when test="$corporate_style = 'compaq'">
							<xsl:attribute name="class">cpqUdrline</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="class">udrline</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:attribute name="href">
                		<xsl:value-of select="@address" />
            		</xsl:attribute>
					<xsl:apply-templates mode="concentra_doc" />
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="concentra_doc" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>

	<xsl:template match="url" mode="concentra_doc">
		<xsl:text>&#xA;</xsl:text>
		<xsl:choose>
			<xsl:when test="$printable = 'no'">
				<xsl:element name="a">
					<xsl:choose>
						<xsl:when test="$corporate_style = 'compaq'">
							<xsl:attribute name="class">cpqUdrline</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="class">udrline</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:attribute name="href">
                <xsl:value-of select="@address" />
            </xsl:attribute>
					<xsl:apply-templates mode="concentra_doc" />
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="concentra_doc" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>&#xA;</xsl:text>
	</xsl:template>
	<hptrans:translations>
		<locale cc="all" doc_locale="EN_US" lc="en">
			<translation name="note">NOTE: </translation>
			<translation name="caution">CAUTION: </translation>
			<translation name="warning">WARNING: </translation>
			<translation name="figure">Figure</translation>
			<translation name="issue">ISSUE: </translation>
			<translation name="solution">SOLUTION: </translation>
			<translation name="cause">CAUSE: </translation>
			<translation name="workaround">WORKAROUND: </translation>
			<translation name="summary">SUMMARY: </translation>
			<translation name="question">QUESTION: </translation>
			<translation name="answer">ANSWER: </translation>
		</locale>
		<locale cc="all" doc_locale="ES_ES" lc="es">
			<translation name="note">NOTA: </translation>
			<translation name="caution">NOTA: </translation>
			<translation name="warning">ADVERTENCIA: </translation>
			<translation name="figure">figura</translation>
			<translation name="issue">PROBLEMA: </translation>
			<translation name="solution">SOLUCIÓN: </translation>
			<translation name="cause">CAUSA: </translation>
			<translation name="workaround">SOLUCIONES: </translation>
			<translation name="summary">RESUMEN: </translation>
			<translation name="question">PREGUNTA: </translation>
			<translation name="answer">RESPUESTA: </translation>
		</locale>
		<locale cc="all" doc_locale="DE_DE" lc="de">
			<translation name="note">HINWEIS: </translation>
			<translation name="caution">VORSICHT: </translation>
			<translation name="warning">ACHTUNG: </translation>
			<translation name="figure">Abbildung</translation>
			<translation name="issue">PROBLEM: </translation>
			<translation name="solution">LÖSUNG: </translation>
			<translation name="cause">URSACHE: </translation>
			<translation name="workaround">PROBLEMVERMEIDUNG: </translation>
			<translation name="summary">ZUSAMMENFASSUNG: </translation>
			<translation name="question">FRAGE: </translation>
			<translation name="answer">ANTWORT: </translation>
		</locale>
		<locale cc="all" doc_locale="FR_FR" lc="fr">
			<translation name="note">REMARQUE: </translation>
			<translation name="caution">REMARQUE: </translation>
			<translation name="warning">AVERTISSEMENT: </translation>
			<translation name="figure">Figure</translation>
			<translation name="issue">PROBLEME: </translation>
			<translation name="solution">SOLUTION: </translation>
			<translation name="cause">CAUSE: </translation>
			<translation name="workaround">PROCEDURE DE DEPANNAGE: </translation>
			<translation name="summary">RECAPITULATIF: </translation>
			<translation name="question">QUESTION: </translation>
			<translation name="answer">RESPONSE: </translation>
		</locale>
		<locale cc="all" doc_locale="NL_NL" lc="nl">
			<translation name="note">OPMERKING: </translation>
			<translation name="caution">OPMERKING: </translation>
			<translation name="warning">WAARSCHUWING: </translation>
			<translation name="figure">Figuur</translation>
			<translation name="issue">PROBLEEM: </translation>
			<translation name="solution">OPLOSSING: </translation>
			<translation name="cause">OORZAAK: </translation>
			<translation name="workaround">REMEDIE: </translation>
			<translation name="summary">SAMENVATTING: </translation>
			<translation name="question">VRAAG: </translation>
			<translation name="answer">ANTWOORD: </translation>
		</locale>
		<locale cc="all" doc_locale="PT_PT" lc="pt">
			<translation name="note">NOTA: </translation>
			<translation name="caution">NOTA: </translation>
			<translation name="warning">ADVERTÊNCIA: </translation>
			<translation name="figure">Figura</translation>
			<translation name="issue">PROBLEMA: </translation>
			<translation name="solution">SOLUÇÃO: </translation>
			<translation name="cause">CAUSE: </translation>
			<translation name="workaround">SOLUÇÕES ALTERNATIVAS: </translation>
			<translation name="summary">RESUMO: </translation>
			<translation name="question">PERGUNTA: </translation>
			<translation name="answer">RESPOSTA: </translation>
		</locale>
		<locale cc="pt" doc_locale="PT_PT" lc="pt">
			<translation name="doc_note">Nota: </translation>
			<translation name="doc_caution">Cuidado: </translation>
			<translation name="doc_warning">Aviso: </translation>
			<translation name="doc_figure">Figure</translation>
			<translation name="doc_issue">ISSUE: </translation>
			<translation name="doc_solution">SOLUTION: </translation>
			<translation name="doc_cause">CAUSE: </translation>
			<translation name="doc_workaround">WORKAROUND: </translation>
			<translation name="doc_summary">SUMMARY: </translation>
			<translation name="doc_question">QUESTION: </translation>
			<translation name="doc_answer">ANSWER: </translation>
		</locale>
		<locale cc="all" doc_locale="DA_DK" lc="da">
			<translation name="doc_note">Bemærk: </translation>
			<translation name="doc_caution">Vigtigt: </translation>
			<translation name="doc_warning">Advarsel: </translation>
			<translation name="doc_figure">Figure</translation>
			<translation name="doc_issue">ISSUE: </translation>
			<translation name="doc_solution">SOLUTION: </translation>
			<translation name="doc_cause">CAUSE: </translation>
			<translation name="doc_workaround">WORKAROUND: </translation>
			<translation name="doc_summary">SUMMARY: </translation>
			<translation name="doc_question">QUESTION: </translation>
			<translation name="doc_answer">ANSWER: </translation>
		</locale>
		<locale cc="all" doc_locale="FI_FI" lc="fii">
			<translation name="doc_note">Huom: </translation>
			<translation name="doc_caution">Varoitus: </translation>
			<translation name="doc_warning">Varoitus: </translation>
			<translation name="doc_figure">Figure</translation>
			<translation name="doc_issue">ISSUE: </translation>
			<translation name="doc_solution">SOLUTION: </translation>
			<translation name="doc_cause">CAUSE: </translation>
			<translation name="doc_workaround">WORKAROUND: </translation>
			<translation name="doc_summary">SUMMARY: </translation>
			<translation name="doc_question">QUESTION: </translation>
			<translation name="doc_answer">ANSWER: </translation>
		</locale>
		<locale cc="all" doc_locale="NO_NO" lc="no">
			<translation name="doc_note">Bemerk: </translation>
			<translation name="doc_caution">Grund: </translation>
			<translation name="doc_warning">Advarsel: </translation>
			<translation name="doc_figure">Figure</translation>
			<translation name="doc_issue">ISSUE: </translation>
			<translation name="doc_solution">SOLUTION: </translation>
			<translation name="doc_cause">CAUSE: </translation>
			<translation name="doc_workaround">WORKAROUND: </translation>
			<translation name="doc_summary">SUMMARY: </translation>
			<translation name="doc_question">QUESTION: </translation>
			<translation name="doc_answer">ANSWER: </translation>
		</locale>
		<locale cc="all" doc_locale="KO_KR" lc="ko">
			<translation name="note">참고 : </translation>
			<translation name="caution">참고: </translation>
			<translation name="warning">경고: </translation>
			<translation name="figure">경고</translation>
			<translation name="issue">문제: </translation>
			<translation name="solution">해결방법: </translation>
			<translation name="cause">원인: </translation>
			<translation name="workaround">임시해결법: </translation>
			<translation name="summary">요약: </translation>
			<translation name="question">질문: </translation>
			<translation name="answer">답변: </translation>
		</locale>
		<locale cc="all" doc_locale="JA_JP" lc="ja">
			<translation name="note">補足： </translation>
			<translation name="caution">注意: </translation>
			<translation name="warning">警告: </translation>
			<translation name="figure">図</translation>
			<translation name="issue">問題: </translation>
			<translation name="solution">解決方法: </translation>
			<translation name="cause">原因: </translation>
			<translation name="workaround">対処方法: </translation>
			<translation name="summary">要約: </translation>
			<translation name="question">質問: </translation>
			<translation name="answer">回答: </translation>
		</locale>
		<locale cc="all" doc_locale="ZH_TW" lc="zh-hant">
			<translation name="note">注意 : </translation>
			<translation name="caution">注意: </translation>
			<translation name="warning">警告: </translation>
			<translation name="figure">圖</translation>
			<translation name="issue">爭議的問題: </translation>
			<translation name="solution">解決方案: </translation>
			<translation name="cause">造成: </translation>
			<translation name="workaround">暫時解決方案: </translation>
			<translation name="summary">摘要: </translation>
			<translation name="question">問題: </translation>
			<translation name="answer">答案: </translation>
		</locale>
		<locale cc="hk" doc_locale="ZH_HK" lc="zh-hant">
			<translation name="note">注意 : </translation>
			<translation name="caution">注意: </translation>
			<translation name="warning">警告: </translation>
			<translation name="figure">圖像</translation>
			<translation name="issue">問題: </translation>
			<translation name="solution">解決方案: </translation>
			<translation name="cause">原因 </translation>
			<translation name="workaround">解決方案: </translation>
			<translation name="summary">摘要: </translation>
			<translation name="question">問題: </translation>
			<translation name="answer">答案: </translation>
		</locale>
		<locale cc="all" doc_locale="ZH_CN" lc="zh-hans">
			<translation name="note">注 : </translation>
			<translation name="caution">注意 : </translation>
			<translation name="warning">警告 : </translation>
			<translation name="figure">图 </translation>
			<translation name="issue">故障:  </translation>
			<translation name="solution">
			</translation>
			<translation name="cause">故障:  </translation>
			<translation name="workaround">故障: </translation>
			<translation name="summary">故障:  </translation>
			<translation name="question">问题 : </translation>
			<translation name="answer">回答 :  </translation>
		</locale>
	</hptrans:translations>
</xsl:stylesheet>
