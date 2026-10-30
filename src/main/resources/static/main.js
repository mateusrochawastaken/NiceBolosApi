let ativo = document.getElementById("inicio")
let tab = document.getElementById("landing")
let localScrollY = 0
let comanda = {}

function onClick(event){
    let element = event.currentTarget
    ativo.classList.remove("precionado")
    if(ativo != event.currentTarget)ativo = event.currentTarget;
    element.classList.add("precionado")
}

function continuar(event){
    document.getElementById("fog").style.display = "block"
    document.getElementById("popup").style.display = "block"
}
function fechar(event){
    document.getElementById("fog").style.display = "none"
    document.getElementById("popup").style.display = "none"
}

async function enviar(event){
    event.preventDefault()
    let formData = new FormData(document.getElementById("confirmaForm"))
    let response = await fetch("http://localhost:8080/enviar-comanda?telefone=" + formData.get("telefone"),{
        "body":JSON.stringify(comanda),
        "method":"POST"
    })
    console.log("Status code:"+response.status)
    let texto = await response.text()
    console.log("Response text:"+ texto)
    if(response.headers.get("Content-Type") == "application/json"){
        let bodyJSON = JSON.parse(texto)
        if(bodyJSON["codigoErro"] == 1){
            document.getElementById("status").textContent = "Erro:Esse numero de telefone não existe ou não está no WhatsApp!"
            document.getElementById("status").style.color = "red"
        }
    }else {
        document.getElementById("status").textContent = "Sucesso, enviamos uma mensagem no seu WhatsApp!"
        document.getElementById("status").style.color = "limegreen"
    }
    document.getElementById("status").style.display = "block"
    return response
}

function pedido(event){
    localScrollY = scrollY
    scrollTo(0,0)
    document.body.classList.remove("inicioBody")
    document.body.classList.add("pedidosBody")
    let tabs = document.getElementsByClassName("landing")
    for(tab of tabs){
        tab.style.display = "none"
    }
    tab = document.getElementById("produtos")
    tab.style.display = "inline-block"
    onClick(event)
}

function inicio(event){
    document.body.classList.remove("pedidosBody")
    document.body.classList.add("inicioBody")
    scrollTo(0,localScrollY)
    document.getElementById("produtos").style.display = "none"
    let tabs = document.getElementsByClassName("landing")
    for(tab of tabs){
        tab.style.display = "block"
    }
    onClick(event)
}

function addUnidadePedido(event){
    let parent = event.currentTarget.parentNode
    let num = parent.getElementsByClassName("num")[0]
    num.textContent = Number.parseInt(num.textContent) + 1
    comanda[parent.parentNode.getAttribute("id")] = num.textContent
}

function removeUnidadePedido(event){
    let parent = event.currentTarget.parentNode
    let num = event.currentTarget.parentNode.getElementsByClassName("num")[0]
    let unidades = Number.parseInt(num.textContent)
    if(unidades != 0){
         num.textContent = Number.parseInt(num.textContent) - 1
         comanda[parent.parentNode.getAttribute("id")] = num.textContent
    }
}

function instaClick(){
    window.open("https://www.instagram.com/nicebolos19/")
}
document.getElementById("inicio").addEventListener("click",inicio)
document.getElementById("pedido").addEventListener("click",pedido)
document.getElementById("confirmaForm").addEventListener("submit",enviar)
document.getElementById("continuar").addEventListener("click",continuar)
document.getElementById("fechar").addEventListener("click",fechar)
document.getElementById("insta").addEventListener("click",instaClick)
for(botao of document.getElementsByClassName("operacao")){
    if(botao.value == "+") botao.addEventListener("click",addUnidadePedido)
    else botao.addEventListener("click",removeUnidadePedido)    
}