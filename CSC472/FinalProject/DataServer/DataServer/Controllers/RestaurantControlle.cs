﻿using System.Collections.Generic;
using System.Web.Http;
using DataServer.Models;

namespace DataServer.Controllers
{
    public class RestaurantController : ApiController
    {
        private RestaurantRespository repo = RestaurantRespository.Current;

        public IEnumerable<Restaurant> GetAll()
        {
            return repo.GetList();
        }

        public Restaurant Get(int id)
        {
            return repo.Get(id);
        }

        [HttpPost]
        public Restaurant Create(Restaurant item)
        {
            return repo.Add(item);
        }

        [HttpPut]
        public bool Update(Restaurant item)
        {
            return repo.Update(item);
        }

        public void Delete(int id)
        {
            repo.Remove(id);
        }
	}
}