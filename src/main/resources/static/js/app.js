function statusLabel(status){
  const map={CREATED:'Создан',UPLOADED:'Загружен',CONVERTING:'Конвертация',TRANSCRIBING:'Распознавание',POST_PROCESSING:'Постобработка',ANALYZING:'Анализ',READY:'Готово',ERROR:'Ошибка'};
  return map[status]||status;
}

function autoResizeTextarea(textarea){
  if(!textarea) return;
  textarea.style.height='auto';
  textarea.style.height=textarea.scrollHeight + 'px';
}

function setupProjectPageInteractions(){
  const processForm=document.getElementById('processForm');
  const processBtn=document.getElementById('processBtn');
  const processSpinner=document.getElementById('processSpinner');
  const processBtnText=document.getElementById('processBtnText');
  const processInfo=document.getElementById('processInfo');
  if(processForm){
    processForm.addEventListener('submit',()=>{
      processBtn.disabled=true;
      processSpinner.classList.remove('d-none');
      processBtnText.textContent='Обработка...';
      processInfo.classList.remove('d-none');
    });
  }

  const processedText=document.getElementById('processedText');
  if(processedText){
    autoResizeTextarea(processedText);
    processedText.addEventListener('input',()=>autoResizeTextarea(processedText));
  }
}

function pollStatus(projectId){
  const badge=document.getElementById('statusBadge');
  const indicator=document.getElementById('processingIndicator');
  const stage=document.getElementById('currentStage');
  const errorAlert=document.getElementById('errorAlert');
  if(!badge) return;
  setInterval(()=>{fetch(`/projects/${projectId}/status`).then(r=>r.json()).then(d=>{
    const label=d.statusLabel || statusLabel(d.status);
    badge.innerText=label;
    if(stage) stage.innerText=label;
    const isRunning=['CONVERTING','TRANSCRIBING','POST_PROCESSING','ANALYZING'].includes(d.status);
    if(indicator) indicator.classList.toggle('active', isRunning);
    if(errorAlert && d.status==='ERROR' && d.error){errorAlert.classList.remove('d-none');}
  });},3000);
}

document.addEventListener('DOMContentLoaded', setupProjectPageInteractions);
