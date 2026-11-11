const APIURL = "http://localhost:8080"
let tipo = "Salgados"
let comanda ={}
let json

async function obterCardapio(){
    let response = await fetch("http://localhost:8080/obter-cardapio")
    json = await response.json()
    console.log("Cardapio obtido na API.")
    console.log(json);
}
function renderizaTipo(){
    for(let v of Object.keys(json.Cardapio[tipo])){
        let produto = document.createElement("div")
        produto.setAttribute("class", "produto")
        let thumb = document.createElement("img")
        thumb.setAttribute("src",APIURL + "/thumbs/" + json.Cardapio[tipo][v]["thumb"])
        let h1 = document.createElement("h1")
        h1.textContent = v
        let h2 = document.createElement("h2")
        h2.textContent = "Preço:" + json.Cardapio[tipo][v]["preco"] + "R$"
        let qtdade = document.createElement("div")
        qtdade.setAttribute("class","qtdade")
        let menos = document.createElement("input")
        let mais = document.createElement("input")
        menos.setAttribute("type","button")
        mais.setAttribute("type","button")
        menos.setAttribute("value","-")
        mais.setAttribute("value","+")
        menos.addEventListener("click",removeUnidadePedido)
        mais.addEventListener("click",addUnidadePedido)
        let text = document.createElement("input")
        text.setAttribute("type","text")
        text.setAttribute("value", "0");
        text.addEventListener("input",quantidadeAlterada)
        qtdade.append(menos,text,mais)
        produto.append(thumb)
        produto.appendChild(h1)
        produto.appendChild(h2)
        produto.appendChild(qtdade)
        document.getElementById(tipo.toLowerCase()).appendChild(produto)
    }
    console.log("Produtos renderizados")
}
function tipoClick(element){
    if(element.currentTarget.value == tipo) return
    document.getElementById(tipo.toLowerCase()).setAttribute("class","produtosNone")
    console.log(element.currentTarget)
    tipo = element.currentTarget.getAttribute("value")
    document.getElementById(tipo.toLowerCase()).setAttribute("class","produtos")
    if(!document.getElementById(tipo.toLowerCase()).hasChildNodes()) {
        renderizaTipo()
    }
}
async function enviar(event){
    event.preventDefault()
    let formData = new FormData(document.getElementById("confirmaForm"))
    console.log(JSON.stringify(comanda))
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
    document.getElementById("status").style.fontSize = "x-large"
    document.getElementById("status").style.margin = 0
    document.getElementById("status").style.display = "block"
    return response
}

function addUnidadePedido(event){
    let parent = event.currentTarget.parentNode
    let num = parent.querySelector("input[type=\"text\"]")
    num.value = Number.parseInt(num.value) + 1
    if(!Object.hasOwn(comanda,tipo)) comanda[tipo] = {}
    comanda[tipo][parent.parentElement.getElementsByTagName("h1")[0].textContent] = num.value
    console.log(JSON.stringify(comanda))
}

function removeUnidadePedido(event){
    let parent = event.currentTarget.parentNode
    let num = parent.querySelector("input[type=\"text\"]")
    let unidades = Number.parseInt(num.value)
    if(unidades != 0){
        num.value = Number.parseInt(num.value) - 1
        comanda[tipo][parent.parentElement.getElementsByTagName("h1")[0].textContent] = num.value
        console.log(JSON.stringify(comanda))
    }
}
function quantidadeAlterada(event){
    let target = event.currentTarget
    if(!Object.hasOwn(comanda,tipo)) comanda[tipo] = {}
    comanda[tipo][target.parentElement.parentElement.getElementsByTagName("h1")[0].textContent] = target.value
    console.log(JSON.stringify(comanda))
}

function continuar(event){
    document.getElementById("fog").style.display = "block"
    document.getElementById("popup").style.display = "block"
}
function fechar(event){
    document.getElementById("fog").style.display = "none"
    document.getElementById("popup").style.display = "none"
}

for(let n of document.getElementById("tipo").childNodes){
    n.addEventListener("click",tipoClick)
}

document.getElementById("confirmaForm").addEventListener("submit",enviar)
document.getElementById("continuar").addEventListener("click",continuar)
document.getElementById("fechar").addEventListener("click",fechar)
obterCardapio().then(renderizaTipo)