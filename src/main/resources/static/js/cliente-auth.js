// cliente-auth.js
(function () {
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => Array.from(r.querySelectorAll(s));

  // =============== Helpers de máscara & validação ===============

  // Mantém só dígitos
  const onlyDigits = (v) => (v || "").replace(/\D/g, "");

  // Máscara CPF: 000.000.000-00
  function maskCPF(value) {
    const v = onlyDigits(value).slice(0, 11);
    const parts = [];
    if (v.length > 3) parts.push(v.substring(0, 3));
    if (v.length > 6) parts.push(v.substring(3, 6));
    if (v.length > 9) parts.push(v.substring(6, 9));
    let masked = "";
    if (v.length <= 3) masked = v;
    else if (v.length <= 6) masked = `${v.substring(0, 3)}.${v.substring(3)}`;
    else if (v.length <= 9) masked = `${v.substring(0, 3)}.${v.substring(3, 6)}.${v.substring(6)}`;
    else masked = `${parts[0]}.${parts[1]}.${parts[2]}-${v.substring(9)}`;
    return masked;
  }

  // Validação CPF (mesma lógica do back)
  function cpfIsValid(cpf) {
    const c = onlyDigits(cpf);
    if (!c || c.length !== 11) return false;
    if (/^(\d)\1{10}$/.test(c)) return false;

    let d1 = 0, d2 = 0;
    for (let i = 0; i < 9; i++) {
      const dig = Number(c[i]);
      d1 += dig * (10 - i);
      d2 += dig * (11 - i);
    }
    let r1 = d1 % 11;
    r1 = r1 < 2 ? 0 : 11 - r1;
    d2 += r1 * 2;
    let r2 = d2 % 11;
    r2 = r2 < 2 ? 0 : 11 - r2;
    return Number(c[9]) === r1 && Number(c[10]) === r2;
  }

  // Máscara CEP: 00000-000 (apenas formato visual; backend usa só dígitos)
  function maskCEP(value) {
    const v = onlyDigits(value).slice(0, 8);
    if (v.length <= 5) return v;
    return `${v.substring(0, 5)}-${v.substring(5)}`;
  }

  // CEP válido = 8 dígitos e não retorna erro no ViaCEP
  async function cepIsValid(cep) {
    const raw = onlyDigits(cep);
    if (raw.length !== 8) return false;
    try {
      const r = await fetch(`https://viacep.com.br/ws/${raw}/json/`);
      const j = await r.json();
      return !j.erro;
    } catch {
      return false;
    }
  }

  // Preenche campos a partir do ViaCEP (prefix = fat | ent1 | dinamico via blocos)
  async function fillFromViaCEP(cepRaw, fillMap) {
    const raw = onlyDigits(cepRaw);
    if (raw.length !== 8) return;
    try {
      const r = await fetch(`https://viacep.com.br/ws/${raw}/json/`);
      const j = await r.json();
      if (j.erro) return;
      if (fillMap.logradouro) fillMap.logradouro.value = j.logradouro || "";
      if (fillMap.bairro) fillMap.bairro.value = j.bairro || "";
      if (fillMap.cidade) fillMap.cidade.value = j.localidade || "";
      if (fillMap.uf) fillMap.uf.value = (j.uf || "").substring(0, 2).toUpperCase();
    } catch {}
  }

  // Mostra feedback simples no input (classe inválida)
  function markValidity(input, ok, messageIfNotOk) {
    if (!input) return;
    if (ok) {
      input.setCustomValidity("");
      input.classList.remove("input-error");
      input.title = "";
    } else {
      input.setCustomValidity(messageIfNotOk || "Inválido");
      input.classList.add("input-error");
      if (messageIfNotOk) input.title = messageIfNotOk;
    }
  }

  // Força uppercase em UF
  function enforceUF(input) {
    input.value = (input.value || "").toUpperCase().slice(0, 2);
  }

  // =============== Hooks de máscara (CPF/CEP) fixos ===============

  const cpfInput = $('#cpf');
  if (cpfInput) {
    cpfInput.addEventListener('input', () => {
      const cursorPos = cpfInput.selectionStart;
      cpfInput.value = maskCPF(cpfInput.value);
      // (opcional) não vamos tentar manter cursor perfeito; a máscara é simples
    });
    cpfInput.addEventListener('blur', () => {
      const ok = cpfIsValid(cpfInput.value);
      markValidity(cpfInput, ok, "CPF inválido");
    });
  }

  function attachCepMaskAndLookup(input, fillMapFactory) {
    if (!input) return;
    input.addEventListener('input', () => {
      input.value = maskCEP(input.value);
      // tirar invalid flag enquanto digita
      if (onlyDigits(input.value).length < 8) {
        markValidity(input, true);
      }
    });
    input.addEventListener('blur', async () => {
      const raw = onlyDigits(input.value);
      const okLen = raw.length === 8;
      if (!okLen) {
        markValidity(input, false, "CEP deve ter 8 dígitos");
        return;
      }
      // valida no ViaCEP
      const okViaCep = await cepIsValid(input.value);
      markValidity(input, okViaCep, "CEP não encontrado");
      if (okViaCep) {
        const map = fillMapFactory ? fillMapFactory() : null;
        if (map) await fillFromViaCEP(input.value, map);
      }
    });
  }

  // Faturamento
  attachCepMaskAndLookup($('#fatCep'), () => ({
    logradouro: $('input[name="fatLogradouro"]'),
    bairro: $('input[name="fatBairro"]'),
    cidade: $('input[name="fatCidade"]'),
    uf: $('input[name="fatUf"]')
  }));

  // Entrega 1
  attachCepMaskAndLookup($('#ent1Cep'), () => ({
    logradouro: $('input[name="ent1Logradouro"]'),
    bairro: $('input[name="ent1Bairro"]'),
    cidade: $('input[name="ent1Cidade"]'),
    uf: $('input[name="ent1Uf"]')
  }));

  // Força uppercase em UFs fixos
  const ufFixos = ['fatUf', 'ent1Uf'].map(id => $('#' + id)).filter(Boolean);
  ufFixos.forEach(inp => inp.addEventListener('input', () => enforceUF(inp)));

  // =============== Copiar faturamento → entrega 1 ===============
  const copiar = $("#copiarFatParaEnt");
  if (copiar) {
    copiar.addEventListener("click", () => {
      const map = ["Cep","Logradouro","Numero","Complemento","Bairro","Cidade","Uf"];
      map.forEach(c => {
        const src = $(`input[name="fat${c}"]`);
        const dst = $(`input[name="ent1${c}"]`);
        if (src && dst) {
          dst.value = src.value;
          if (c === "Cep") dst.value = maskCEP(src.value);
          if (c === "Uf") enforceUF(dst);
        }
      });
      // tenta preencher via CEP caso necessário
      const ent1Cep = $('input[name="ent1Cep"]');
      if (ent1Cep && onlyDigits(ent1Cep.value).length === 8) {
        fillFromViaCEP(ent1Cep.value, {
          logradouro: $('input[name="ent1Logradouro"]'),
          bairro: $('input[name="ent1Bairro"]'),
          cidade: $('input[name="ent1Cidade"]'),
          uf: $('input[name="ent1Uf"]')
        });
      }
    });
  }

  // =============== Entregas extras dinâmicas ===============
  const extras = $("#extras");
  const addEntrega = $("#addEntrega");

  function buildEntregaExtraBlock() {
    const wrap = document.createElement("fieldset");
    wrap.className = "card-block";
    wrap.innerHTML = `
      <legend>Entrega adicional</legend>
      <div class="form-grid cols-4">
        <div class="field">
          <label>CEP</label>
          <input type="text" name="entCep[]" class="cep extra" maxlength="9" placeholder="00000-000" required />
        </div>
        <div class="field col-2">
          <label>Logradouro</label>
          <input type="text" name="entLogradouro[]" required />
        </div>
        <div class="field">
          <label>Número</label>
          <input type="text" name="entNumero[]" required />
        </div>
        <div class="field">
          <label>Complemento</label>
          <input type="text" name="entComplemento[]" />
        </div>
        <div class="field">
          <label>Bairro</label>
          <input type="text" name="entBairro[]" required />
        </div>
        <div class="field">
          <label>Cidade</label>
          <input type="text" name="entCidade[]" required />
        </div>
        <div class="field">
          <label>UF</label>
          <input type="text" name="entUf[]" maxlength="2" required />
        </div>
      </div>
      <div class="inline-actions">
        <button type="button" class="btn outline js-remove-entrega">Remover</button>
      </div>
    `;

    // liga máscara + viaCEP ao CEP desse bloco
    const cepInput = $('input[name="entCep[]"]', wrap);
    const ufInput  = $('input[name="entUf[]"]', wrap);
    attachCepMaskAndLookup(cepInput, () => ({
      logradouro: $('input[name="entLogradouro[]"]', wrap),
      bairro: $('input[name="entBairro[]"]', wrap),
      cidade: $('input[name="entCidade[]"]', wrap),
      uf: $('input[name="entUf[]"]', wrap)
    }));
    if (ufInput) ufInput.addEventListener('input', () => enforceUF(ufInput));

    // remover bloco
    $('.js-remove-entrega', wrap).addEventListener('click', () => wrap.remove());

    return wrap;
  }

  if (addEntrega && extras) {
    addEntrega.addEventListener("click", () => {
      extras.appendChild(buildEntregaExtraBlock());
    });
  }

  // =============== Validações finais no submit ===============
  const form = $("#formCadastro");
  if (form) {
    form.addEventListener("submit", async (e) => {
      // Confirmar senha
      const senha = form.querySelector('input[name="senha"]')?.value || "";
      const conf  = $('#confirmarSenha')?.value || "";
      if (senha !== conf) {
        e.preventDefault();
        alert("As senhas não conferem.");
        return;
      }

      // Nome: 2 palavras, >=3 letras cada
      const nome = form.querySelector('input[name="nomeCompleto"]')?.value?.trim() || "";
      const partes = nome.split(/\s+/);
      const nomeOk = partes.length >= 2 && partes.every(p => (p.replace(/[^A-Za-zÀ-ÖØ-öø-ÿ]/g, "").length >= 3));
      if (!nomeOk) {
        e.preventDefault();
        alert("Nome deve ter pelo menos 2 palavras, com 3+ letras cada.");
        return;
      }

      // CPF
      const cpfEl = $('#cpf');
      if (cpfEl) {
        cpfEl.value = maskCPF(cpfEl.value); // garante máscara final
        const ok = cpfIsValid(cpfEl.value);
        markValidity(cpfEl, ok, "CPF inválido");
        if (!ok) {
          e.preventDefault();
          alert("CPF inválido.");
          return;
        }
      }

      // CEPs obrigatórios: faturamento + entrega 1
      const fatCep = $('#fatCep');
      const ent1Cep = $('#ent1Cep');

      async function ensureCepValid(input, label) {
        if (!input) return true;
        input.value = maskCEP(input.value);
        const raw = onlyDigits(input.value);
        if (raw.length !== 8) {
          markValidity(input, false, "CEP deve ter 8 dígitos");
          alert(`CEP de ${label} inválido (8 dígitos).`);
          return false;
        }
        const ok = await cepIsValid(input.value);
        markValidity(input, ok, "CEP não encontrado");
        if (!ok) alert(`CEP de ${label} não encontrado.`);
        return ok;
      }

      // valida sequencialmente para poder exibir alertas específicos
      if (!(await ensureCepValid(fatCep, "faturamento"))) { e.preventDefault(); return; }
      if (!(await ensureCepValid(ent1Cep, "entrega"))) { e.preventDefault(); return; }

      // CEPs dos extras
      const extraCepInputs = $$('input[name="entCep[]"]');
      for (let i = 0; i < extraCepInputs.length; i++) {
        const ok = await ensureCepValid(extraCepInputs[i], `entrega adicional #${i+1}`);
        if (!ok) { e.preventDefault(); return; }
      }

      // UF uppercase geral (faturamento + entrega1 + extras)
      ['fatUf','ent1Uf'].forEach(id => { const inp = $('#'+id); if (inp) enforceUF(inp); });
      $$('input[name="entUf[]"]').forEach(enforceUF);

      // tudo ok → deixa enviar
    });
  }

  // =============== Estilo visual de erro (opcional, combina com seu CSS) ===============
  const style = document.createElement('style');
  style.textContent = `
    .input-error { border-color: #ef4444 !important; box-shadow: 0 0 0 4px rgba(239,68,68,.25) !important; }
  `;
  document.head.appendChild(style);
})();
