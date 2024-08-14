

#include "http_handler.h"

namespace httpserver{


static void
async_response_callback_wrap(request_t* req, response_t* resp) {
    //std::cout << "async_response_callback_wrap" << std::endl;
    auto handler = static_cast<http_handler *>(req->handler_);
    auto callback = handler->callback_response();
    callback(resp);
    response_free(resp);
}

http_handler::http_handler(
    http_handler_callback_t http_handler_callback,
    http_handler_async_callback_t http_handler_async_callback)
    :http_handler_callback_(http_handler_callback),
     http_handler_async_callback_(http_handler_async_callback)
{
    use_async_ = http_handler_async_callback_ != NULL;
}


bool
http_handler::use_async(){
    return use_async_;
}

void
http_handler::use_async(bool b){
    use_async_ = b;
}

response_t* http_handler::dispatch(request_t* req) {
    return (*http_handler_callback_)(req);
}

void http_handler::dispatch_async(request_t* req, std::function<callback_t<response_t*>> callback) {
    callback_response_ = callback;
    req->handler_ = this;
    (*http_handler_async_callback_)(req, &async_response_callback_wrap);
}


std::function<http_handler::callback_t<response_t*>>
http_handler::callback_response(){
    return callback_response_;
}

}
