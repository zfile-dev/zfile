import{_ as I,b as f,d as t,N as O,I as o}from"./base.cf6a8745.js";import{u as y,a as S}from"./popper.0433c431.js";import{g as C,r as E,v as _,E as b,o as L,u as m,H as $}from"./index.b54e4b70.js";const h={inheritAttrs:!1};function M(n,l,c,s,a,u){return C(n.$slots,"default")}var w=I(h,[["render",M],["__file","/home/runner/work/element-plus/element-plus/packages/components/collection/src/collection.vue"]]);const v={name:"ElCollectionItem",inheritAttrs:!1};function P(n,l,c,s,a,u){return C(n.$slots,"default")}var A=I(v,[["render",P],["__file","/home/runner/work/element-plus/element-plus/packages/components/collection/src/collection-item.vue"]]);const B="data-el-collection-item",K=n=>{const l=`El${n}Collection`,c=`${l}Item`,s=Symbol(l),a=Symbol(c),u={...w,name:l,setup(){const d=E(null),i=new Map;_(s,{itemMap:i,getItems:()=>{const r=m(d);if(!r)return[];const e=Array.from(r.querySelectorAll(`[${B}]`));return[...i.values()].sort((g,N)=>e.indexOf(g.ref)-e.indexOf(N.ref))},collectionRef:d})}},T={...A,name:c,setup(d,{attrs:i}){const p=E(null),r=b(s,void 0);_(a,{collectionItemRef:p}),L(()=>{const e=m(p);e&&r.itemMap.set(e,{ref:e,...i})}),$(()=>{const e=m(p);r.itemMap.delete(e)})}};return{COLLECTION_INJECTION_KEY:s,COLLECTION_ITEM_INJECTION_KEY:a,ElCollection:u,ElCollectionItem:T}},R=f({trigger:y.trigger,effect:{...S.effect,default:"light"},type:{type:t(String)},placement:{type:t(String),default:"bottom"},popperOptions:{type:t(Object),default:()=>({})},id:String,size:{type:String,default:""},splitButton:Boolean,hideOnClick:{type:Boolean,default:!0},loop:{type:Boolean,default:!0},showTimeout:{type:Number,default:150},hideTimeout:{type:Number,default:150},tabindex:{type:t([Number,String]),default:0},maxHeight:{type:t([Number,String]),default:""},popperClass:{type:String,default:""},disabled:{type:Boolean,default:!1},role:{type:String,default:"menu"},buttonProps:{type:t(Object)}}),D=f({command:{type:[Object,String,Number],default:()=>({})},disabled:Boolean,divided:Boolean,textValue:String,icon:{type:O}}),H=f({onKeydown:{type:t(Function)}}),x=[o.down,o.pageDown,o.home],Y=[o.up,o.pageUp,o.end],U=[...x,...Y],{ElCollection:V,ElCollectionItem:q,COLLECTION_INJECTION_KEY:z,COLLECTION_ITEM_INJECTION_KEY:G}=K("Dropdown");export{G as C,V as E,U as F,Y as L,D as a,B as b,K as c,R as d,q as e,H as f,z as g};